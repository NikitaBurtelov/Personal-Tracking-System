package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.entity.ProcessingRequest;
import org.pts.document.storage.model.enums.Status;
import org.pts.document.storage.model.enums.Type;
import org.pts.document.storage.repository.DocumentRepository;
import org.pts.document.storage.repository.OutboxItemRepository;
import org.pts.document.storage.repository.OutboxRepository;
import org.pts.document.storage.repository.ProcessingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobManagerServiceImpl implements JobManagerService {
    private final RequestManagerService requestManagerService;
    private final DocumentRepository documentRepository;
    private final ProcessingRequestRepository processingRequestRepository;
    private final OutboxRepository outboxRepository;
    private final OutboxItemRepository outboxItemRepository;

    @Transactional
    @Override
    public void createGetDocumentJob(GetDocumentSourceRequest msg) {
        try {
            var batches = chunk(msg.s3Keys(), 10);
            var jobIds = new ArrayList<Long>();

            for (var batch : batches) {
                var batchDocuments = documentRepository.findAllByKeyIn(batch);

                var job = OutboxJobEntity.builder()
                        .type(Type.GET)
                        .status(Status.NEW)
                        .build();

                job = outboxRepository.save(job);
                jobIds.add(job.getId());

                List<OutboxJobItemEntity> items = new ArrayList<>(Collections.emptyList());

                for (var batchDocument : batchDocuments) {
                    var jobItem = OutboxJobItemEntity.builder()
                            .jobId(job.getId())
                            .documentId(batchDocument.getId())
                            .status(Status.NEW)
                            .build();

                    items.add(jobItem);
                }

                outboxItemRepository.saveAll(items);
            }

            log.info("Tasks created: {} for request:{}", jobIds, msg.workId());
        } catch (Exception e) {
            log.error("Failed to create tasks for request:{}", msg.workId());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public void createUploadDocumentJob(UploadDocumentCommand msg) {
        try {
            if (processingRequestRepository.findById(msg.workId()).isPresent()) {
                return;
            }

            List<UploadDocumentCommand.PayloadDocumentsUpload.Document> docs = msg.payload().documents();

            var batches = chunk(docs, 10);

            var processingRequestId = UUID.randomUUID();
            var allDocuments = new ArrayList<DocumentEntity>();
            var allJobItems = new ArrayList<OutboxJobItemEntity>();
            var jobIds = new ArrayList<Long>();

            for (List<UploadDocumentCommand.PayloadDocumentsUpload.Document> batch : batches) {

                var job = OutboxJobEntity.builder()
                        .requestId(processingRequestId)
                        .type(Type.UPLOAD)
                        .status(Status.NEW)
                        .build();

                job = outboxRepository.save(job);
                jobIds.add(job.getId());

                for (var doc : batch) {

                    var documentId = UUID.randomUUID();

                    var document = DocumentEntity.builder()
                            .id(documentId)
                            .tempKey(doc.s3TempKey())
                            .tempBucket(doc.bucket())
                            .status(Status.NEW)
                            .build();

                    allDocuments.add(document);

                    var jobItem = OutboxJobItemEntity.builder()
                            .jobId(job.getId())
                            .documentId(documentId)
                            .status(Status.NEW)
                            .build();

                    allJobItems.add(jobItem);
                }
            }

            var processingRequest = ProcessingRequest.builder()
                    .id(UUID.randomUUID())
                    .totalJobs(allJobItems.size())
                    .type(Type.UPLOAD)
                    .build();

            processingRequestRepository.save(processingRequest);
            documentRepository.saveAll(allDocuments);
            outboxItemRepository.saveAll(allJobItems);

            log.info("Tasks created: {} for request:{}", jobIds, msg.workId());
        } catch (Exception e) {
            log.error("Failed to create tasks for request:{}", msg.workId());
            throw new RuntimeException(e);
        }

    }

    @Transactional
    @Override
    public Map<OutboxJobEntity, List<OutboxJobItemEntity>> takeForProcessing(
            Type type,
            Status status,
            int limit
    ) {
        try {
            var jobs = outboxRepository.findAllByTypeAndStatus(
                    type.getType(),
                    status.getStatus(),
                    limit
            );

            jobs.forEach(job -> job.setStatus(Status.PROCESSING));

            var items = outboxItemRepository
                    .findAllByJobIdInAndStatusContains(
                            jobs.stream()
                                    .map(OutboxJobEntity::getId)
                                    .toList(),
                            Status.NEW
                    );

            items.forEach(item -> item.setStatus(status));

            Map<Long, List<OutboxJobItemEntity>> idsItemsMap = items
                    .stream()
                    .collect(Collectors.groupingBy(OutboxJobItemEntity::getJobId));

            return jobs.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            job -> idsItemsMap.getOrDefault(job.getId(), List.of())
                    ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public void updateJobAndItemStatus(
            Long jobId,
            Status jobStatus,
            Map<Long, Status> itemsStatusMap
    ) {
        outboxRepository.updateStatus(jobId, jobStatus);

        Map<Status, List<Long>> grouped = itemsStatusMap.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));


        grouped.forEach((groupedStatus, ids) -> {
            outboxItemRepository.updateStatus(ids, groupedStatus);
        });
    }

    @Transactional
    @Override
    public void updateJobAndItemStatus(
            OutboxJobEntity job,
            Status jobStatus,
            Map<Long, Status> itemsStatusMap
    ) {
        outboxRepository.updateStatus(job.getId(), jobStatus);

        Map<Status, List<Long>> grouped = itemsStatusMap.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));


        grouped.forEach((groupedStatus, ids) -> {
            outboxItemRepository.updateStatus(ids, groupedStatus);
        });

        requestManagerService.onJobCompleted(job.getRequestId());
    }

    @Transactional
    @Override
    public void updateJobAndItemStatus(
            Long jobId,
            List<Long> itemsId,
            Status status
    ) {
        var updatedOutboxField = outboxRepository.updateStatus(jobId, status);

        if (updatedOutboxField == 0) {
            throw new IllegalStateException(
                    "Failed to update outbox job. jobId=" + jobId
            );
        }

        var updatedItemField = outboxItemRepository.updateStatus(itemsId, status);

        if (updatedItemField != itemsId.size()) {
            throw new IllegalStateException(
                    "Expected to update " + itemsId.size() +
                            " outbox items but updated " + updatedItemField
            );
        }
    }

    @Transactional
    @Override
    public void updateJobAndItemStatus(
            List<Long> jobsId,
            List<Long> itemsId,
            Status status
    ) {
        var updatedOutboxField = outboxRepository.updateStatus(jobsId, status);

        if (updatedOutboxField == 0) {
            throw new IllegalStateException(
                    "Failed to update outbox job. jobsId=" + jobsId
            );
        }

        var updatedItemsField = outboxItemRepository.updateStatus(itemsId, status);

        if (updatedItemsField != itemsId.size()) {
            throw new IllegalStateException(
                    "Expected to update " + itemsId.size() +
                            " outbox items but updated " + updatedItemsField
            );
        }
    }

    @Transactional
    @Override
    public void updateJobAndItemStatusBatch(
            Map<Long, Status> jobStatusMap,
            Map<Long, Map<Long, Status>> itemsStatusByJob
    ) {
        try {
            Map<Status, List<Long>> jobsGrouped = jobStatusMap.entrySet()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Map.Entry::getValue,
                            Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                    ));

            jobsGrouped.forEach((status, ids) -> {
                if (ids != null && !ids.isEmpty()) {
                    outboxRepository.updateStatus(ids, status);
                }
            });

            Map<Status, List<Long>> itemsGrouped = new HashMap<>();
            for (var entry : itemsStatusByJob.entrySet()) {
                var perJobMap = entry.getValue();
                if (perJobMap == null) continue;
                for (var itemEntry : perJobMap.entrySet()) {
                    var itemId = itemEntry.getKey();
                    var itemStatus = itemEntry.getValue();
                    itemsGrouped.computeIfAbsent(itemStatus, k -> new ArrayList<>()).add(itemId);
                }
            }

            itemsGrouped.forEach((status, ids) -> {
                if (ids != null && !ids.isEmpty()) {
                    outboxItemRepository.updateStatus(ids, status);
                }
            });

            var doneJobIds = jobStatusMap.entrySet().stream()
                    .filter(e -> e.getValue() == Status.DONE)
                    .map(Map.Entry::getKey)
                    .toList();

            if (!doneJobIds.isEmpty()) {
                var doneJobs = outboxRepository.findAllById(doneJobIds);
                doneJobs.forEach(job -> requestManagerService.onJobCompleted(job.getRequestId()));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TODO убрать всю работу с document d DocService
    @Override
    public List<DocumentEntity> getDocument(UUID requestId) {
        var jobs = outboxRepository.findAllByRequestId(requestId);
        var items = outboxItemRepository.findAllByJobIdIn(
                jobs.stream()
                        .map(OutboxJobEntity::getId)
                        .toList()
        );

        return documentRepository.
                findAllByIdIn(
                        items.stream()
                                .map(OutboxJobItemEntity::getDocumentId)
                                .toList()
                );
    }

    private <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }

        return result;
    }
}
