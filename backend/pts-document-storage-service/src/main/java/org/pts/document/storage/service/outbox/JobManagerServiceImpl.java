package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.entity.ProcessingRequest;
import org.pts.document.storage.model.enums.DocumentStatus;
import org.pts.document.storage.model.enums.OutboxJobStatus;
import org.pts.document.storage.model.enums.OutboxJobType;
import org.pts.document.storage.model.enums.RequestType;
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
                        .type(OutboxJobType.GET)
                        .status(OutboxJobStatus.NEW)
                        .build();

                job = outboxRepository.save(job);
                jobIds.add(job.getId());

                List<OutboxJobItemEntity> items = new ArrayList<>(Collections.emptyList());

                for (var batchDocument : batchDocuments) {
                    var jobItem = OutboxJobItemEntity.builder()
                            .jobId(job.getId())
                            .documentId(batchDocument.getId())
                            .status(OutboxJobStatus.NEW)
                            .build();

                    items.add(jobItem);
                }

                outboxItemRepository.saveAll(items);
            }

            log.info("Tasks created: {} for request:{}", jobIds, msg.workId());
        } catch(Exception e) {
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
                        .type(OutboxJobType.UPLOAD)
                        .status(OutboxJobStatus.NEW)
                        .build();

                job = outboxRepository.save(job);
                jobIds.add(job.getId());

                for (var doc : batch) {

                    var documentId = UUID.randomUUID();

                    var document = DocumentEntity.builder()
                            .id(documentId)
                            .tempKey(doc.s3TempKey())
                            .tempBucket(doc.bucket())
                            .status(DocumentStatus.NEW)
                            .build();

                    allDocuments.add(document);

                    var jobItem = OutboxJobItemEntity.builder()
                            .jobId(job.getId())
                            .documentId(documentId)
                            .status(OutboxJobStatus.NEW)
                            .build();

                    allJobItems.add(jobItem);
                }
            }

            var processingRequest = ProcessingRequest.builder()
                    .id(UUID.randomUUID())
                    .totalJobs(allJobItems.size())
                    .type(RequestType.UPLOAD)
                    .build();

            processingRequestRepository.save(processingRequest);
            documentRepository.saveAll(allDocuments);
            outboxItemRepository.saveAll(allJobItems);

            log.info("Tasks created: {} for request:{}", jobIds, msg.workId());
        } catch(Exception e) {
            log.error("Failed to create tasks for request:{}", msg.workId());
            throw new RuntimeException(e);
        }

    }

    @Transactional
    @Override
    public Map<OutboxJobEntity, List<OutboxJobItemEntity>> takeForProcessing(
            OutboxJobType type,
            OutboxJobStatus status,
            int limit
    ) {
        try {
            var jobs = outboxRepository.findAllByTypeAndStatus(
                    type.getType(),
                    status.getStatus(),
                    limit
            );

            jobs.forEach(job -> job.setStatus(OutboxJobStatus.PROCESSING));

            var items = outboxItemRepository
                    .findAllByJobIdInAndStatusContains(
                            jobs.stream()
                                    .map(OutboxJobEntity::getId)
                                    .toList(),
                            OutboxJobStatus.NEW
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
            throw  new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public void updateJobAndItemStatus(
            Long jobId,
            OutboxJobStatus jobStatus,
            Map<Long, OutboxJobStatus> itemsStatusMap
    ) {
        outboxRepository.updateStatus(jobId, jobStatus);

        Map<OutboxJobStatus, List<Long>> grouped = itemsStatusMap.entrySet().stream()
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
            OutboxJobStatus jobStatus,
            Map<Long, OutboxJobStatus> itemsStatusMap
    ) {
        outboxRepository.updateStatus(job.getId(), jobStatus);

        Map<OutboxJobStatus, List<Long>> grouped = itemsStatusMap.entrySet().stream()
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
            OutboxJobStatus status
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
            OutboxJobStatus status
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

    private <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }

        return result;
    }
}
