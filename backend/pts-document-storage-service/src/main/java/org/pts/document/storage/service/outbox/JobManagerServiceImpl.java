package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.entity.ProcessingOperation;
import org.pts.document.storage.model.enums.DocumentStatus;
import org.pts.document.storage.model.enums.JobStatus;
import org.pts.document.storage.model.enums.JobType;
import org.pts.document.storage.repository.DocumentRepository;
import org.pts.document.storage.repository.OutboxItemRepository;
import org.pts.document.storage.repository.OutboxRepository;
import org.pts.document.storage.repository.ProcessingOperationRepository;
import org.pts.document.storage.service.dto.JobContext;
import org.pts.document.storage.service.dto.JobItemContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobManagerServiceImpl implements JobManagerService {
    private final ProcessingOperationManager processingOperationManager;

    //TODO уменьшить ответственность сервиса
    private final DocumentRepository documentRepository;
    private final ProcessingOperationRepository processingOperationRepository;
    private final OutboxRepository outboxRepository;
    private final OutboxItemRepository outboxItemRepository;

    @Transactional
    @Override
    public void createGetDocumentJob(GetDocumentSourceRequest msg) {
        try {
            if (processingOperationRepository.findById(msg.workId()).isPresent()) {
                return;
            }

            var batches = chunk(msg.s3Keys(), 10);
            var jobIds = new ArrayList<Long>();

            for (var batch : batches) {
                var batchDocuments = documentRepository.findAllByKeyIn(batch);

                var job = OutboxJobEntity.builder()
                        .type(JobType.GET)
                        .status(JobStatus.NEW)
                        .build();

                job = outboxRepository.save(job);
                jobIds.add(job.getId());

                var items = new ArrayList<OutboxJobItemEntity>();

                for (var batchDocument : batchDocuments) {
                    var jobItem = OutboxJobItemEntity.builder()
                            .jobId(job.getId())
                            .documentId(batchDocument.getId())
                            .status(JobStatus.NEW)
                            .build();

                    items.add(jobItem);
                }

                outboxItemRepository.saveAll(items);
            }

            var processingRequest = ProcessingOperation.builder()
                    .id(msg.workId())
                    .totalJobs(jobIds.size())
                    .type(JobType.UPLOAD)
                    .build();

            processingOperationRepository.save(processingRequest);

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
            if (processingOperationRepository.findById(msg.workId()).isPresent()) {
                return;
            }

            List<UploadDocumentCommand.PayloadDocumentsUpload.Document> docs = msg.payload().documents();

            var batches = chunk(docs, 10);

            var allDocuments = new ArrayList<DocumentEntity>();
            var allJobItems = new ArrayList<OutboxJobItemEntity>();
            var jobIds = new ArrayList<Long>();

            for (List<UploadDocumentCommand.PayloadDocumentsUpload.Document> batch : batches) {

                var job = OutboxJobEntity.builder()
                        .operationId(msg.workId())
                        .type(JobType.UPLOAD)
                        .status(JobStatus.NEW)
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
                            .status(JobStatus.NEW)
                            .build();

                    allJobItems.add(jobItem);
                }
            }

            var processingRequest = ProcessingOperation.builder()
                    .id(msg.workId())
                    .totalJobs(jobIds.size())
                    .type(JobType.UPLOAD)
                    .build();

            processingOperationRepository.save(processingRequest);
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
    public Map<JobContext, List<JobItemContext>> takeForProcessing(
            JobType type,
            JobStatus status,
            int limit
    ) {
        try {
            var jobs = outboxRepository.findAllByTypeAndStatus(
                    type.getType(),
                    status.getStatus(),
                    limit
            );

            jobs.forEach(job -> job.setStatus(JobStatus.PROCESSING));

            var requests = processingOperationRepository.findAllByIdIn(
                    jobs.stream()
                            .map(OutboxJobEntity::getOperationId)
                            .toList()
            );

            requests.forEach(request -> request.setStatus(JobStatus.PROCESSING));

            var items = outboxItemRepository
                    .findAllByJobIdInAndStatus(
                            jobs.stream()
                                    .map(OutboxJobEntity::getId)
                                    .toList(),
                            JobStatus.NEW
                    );

            items.forEach(item -> item.setStatus(JobStatus.PROCESSING));

            Map<Long, List<JobItemContext>> idsItemsMap = items
                    .stream()
                    .map(item -> {
                        return JobItemContext.builder()
                                .itemId(item.getId())
                                .jobId(item.getJobId())
                                .documentId(item.getDocumentId())
                                .status(item.getStatus())
                                .build();
                    })
                    .collect(Collectors.groupingBy(JobItemContext::jobId));

            return jobs.stream()
                    .map(job -> {
                        return JobContext.builder()
                                .jobId(job.getId())
                                .eventId(job.getOperationId())
                                .type(job.getType())
                                .items(idsItemsMap.get(job.getId()))
                                .build();
                    })
                    .collect(Collectors.toMap(
                            Function.identity(),
                            job -> idsItemsMap.getOrDefault(job.jobId(), List.of())
                    ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public void updateJobAndItemStatus(
            List<Long> jobIds,
            List<Long> itemIds,
            JobStatus status
    ) {
        var updatedRows = outboxRepository.updateStatus(jobIds, status);

        if (updatedRows == 0) {
            throw new IllegalStateException(
                    "Failed to update outbox job. jobIds=" + jobIds
            );
        }

        var updatedItemsCount = outboxItemRepository.updateStatus(itemIds, status);

        if (updatedItemsCount != itemIds.size()) {
            throw new IllegalStateException(
                    "Expected to update " + itemIds.size() +
                            " outbox items but updated " + updatedItemsCount
            );
        }
    }

    @Transactional
    @Override
    public void updateJobAndItemStatusBatch(
            Map<Long, JobStatus> jobStatusMap,
            Map<Long, JobStatus> itemsStatusByJob
    ) {
        try {
            Map<JobStatus, List<Long>> jobsGrouped = jobStatusMap.entrySet()
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

            Map<JobStatus, List<Long>> itemsGrouped = itemsStatusByJob.entrySet()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Map.Entry::getValue,
                            Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                    ));

            itemsGrouped.forEach((status, ids) -> {
                if (ids != null && !ids.isEmpty()) {
                    outboxItemRepository.updateStatus(ids, status);
                }
            });

            var doneJobIds = jobStatusMap.entrySet().stream()
                    .filter(e -> e.getValue() == JobStatus.DONE)
                    .map(Map.Entry::getKey)
                    .toList();

            if (!doneJobIds.isEmpty()) {
                var doneJobs = outboxRepository.findAllById(doneJobIds);
                doneJobs.forEach(job -> processingOperationManager.onJobCompleted(job.getOperationId()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
