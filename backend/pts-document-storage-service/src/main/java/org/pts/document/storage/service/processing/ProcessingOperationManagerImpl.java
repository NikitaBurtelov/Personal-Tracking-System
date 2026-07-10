package org.pts.document.storage.service.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.entity.ProcessingBatchEntity;
import org.pts.document.storage.model.entity.ProcessingOperation;
import org.pts.document.storage.model.entity.ProcessingTaskEntity;
import org.pts.document.storage.model.enums.DocumentStatus;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.pts.document.storage.model.enums.ProcessingType;
import org.pts.document.storage.repository.DocumentRepository;
import org.pts.document.storage.repository.ProcessingBatchRepository;
import org.pts.document.storage.repository.ProcessingOperationRepository;
import org.pts.document.storage.repository.ProcessingTaskRepository;
import org.pts.document.storage.service.dto.BatchContext;
import org.pts.document.storage.service.dto.TaskContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessingOperationManagerImpl implements ProcessingOperationManager {
    private final ProcessingOperationService processingOperationService;

    //TODO уменьшить ответственность сервиса
    private final DocumentRepository documentRepository;
    private final ProcessingOperationRepository processingOperationRepository;
    private final ProcessingBatchRepository batchRepository;
    private final ProcessingTaskRepository taskRepository;

    @Transactional
    @Override
    public void createGetDocumentTask(GetDocumentSourceRequest msg) {
        try {
            if (processingOperationRepository.findById(msg.workId()).isPresent()) {
                log.debug("Processing operation already exists for workId: {}", msg.workId());
                return;
            }

            log.debug("Processing upload document request-id: {} request-payload: {}",
                    msg.workId(),
                    msg.s3Keys().toString());

            var chunks = chunk(msg.s3Keys(), 10);
            var batchIds = new ArrayList<Long>();

            for (var chunk : chunks) {
                var batchDocuments = documentRepository.findAllByKeyIn(chunk);

                var batch = ProcessingBatchEntity.builder()
                        .type(ProcessingType.GET)
                        .status(ProcessingStatus.NEW)
                        .build();

                batch = batchRepository.save(batch);
                batchIds.add(batch.getId());

                var tasks = new ArrayList<ProcessingTaskEntity>();

                for (var batchDocument : batchDocuments) {
                    var task = ProcessingTaskEntity.builder()
                            .batchId(batch.getId())
                            .documentId(batchDocument.getId())
                            .status(ProcessingStatus.NEW)
                            .build();

                    tasks.add(task);
                }

                taskRepository.saveAll(tasks);
            }

            var processingRequest = ProcessingOperation.builder()
                    .id(msg.workId())
                    .totalBatch(batchIds.size())
                    .type(ProcessingType.UPLOAD)
                    .build();

            processingOperationRepository.save(processingRequest);

            log.info("Tasks created: {} for request:{}", batchIds, msg.workId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tasks for request:{}" + msg.workId(), e);
        }
    }

    @Transactional
    @Override
    public void createUploadDocumentTask(UploadDocumentCommand msg) {
        try {
            if (processingOperationRepository.findById(msg.workId()).isPresent()) {
                log.info("Processing operation already exists for workId: {}", msg.workId());
                return;
            }

            log.debug("Processing upload document request-id: {} request-payload: {}",
                    msg.workId(),
                    msg.payload().toString()
            );

            List<UploadDocumentCommand.PayloadDocumentsUpload.Document> docs = msg.payload().documents();

            var chunks = chunk(docs, 10);

            var allDocuments = new ArrayList<DocumentEntity>();
            var allTasks = new ArrayList<ProcessingTaskEntity>();
            var batchIds = new ArrayList<Long>();

            for (List<UploadDocumentCommand.PayloadDocumentsUpload.Document> chunk : chunks) {

                var batch = ProcessingBatchEntity.builder()
                        .operationId(msg.workId())
                        .type(ProcessingType.UPLOAD)
                        .status(ProcessingStatus.NEW)
                        .build();

                batch = batchRepository.save(batch);
                batchIds.add(batch.getId());

                for (var doc : chunk) {

                    var documentId = UUID.randomUUID();

                    var document = DocumentEntity.builder()
                            .id(documentId)
                            .transferObjectKey(doc.s3TempKey())
                            .transferBucket(doc.bucket())
                            .status(DocumentStatus.NEW)
                            .build();

                    allDocuments.add(document);

                    var task = ProcessingTaskEntity.builder()
                            .batchId(batch.getId())
                            .documentId(documentId)
                            .status(ProcessingStatus.NEW)
                            .build();

                    allTasks.add(task);
                }
            }

            var processingRequest = ProcessingOperation.builder()
                    .id(msg.workId())
                    .totalBatch(batchIds.size())
                    .type(ProcessingType.UPLOAD)
                    .build();

            processingOperationRepository.save(processingRequest);
            documentRepository.saveAll(allDocuments);
            taskRepository.saveAll(allTasks);

            log.info("Tasks created: {} for request:{}", batchIds, msg.workId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tasks for request:{}" + msg.workId(), e);
        }

    }

    @Transactional
    @Override
    public Map<BatchContext, List<TaskContext>> takeForProcessing(
            ProcessingType type,
            ProcessingStatus status,
            int limit
    ) {
        try {
            var butches = batchRepository.findAllByTypeAndStatus(
                    type.getType(),
                    status.getStatus(),
                    limit
            );

            log.debug("Found {} butches for processing of type {} and status {}", butches.size(), type, status);

            butches.forEach(job -> job.setStatus(ProcessingStatus.PROCESSING));

            var requests = processingOperationRepository.findAllByIdIn(
                    butches.stream()
                            .map(ProcessingBatchEntity::getOperationId)
                            .toList()
            );

            requests.forEach(request -> request.setStatus(ProcessingStatus.PROCESSING));

            var tasks = taskRepository
                    .findAllByBatchIdInAndStatus(
                            butches.stream()
                                    .map(ProcessingBatchEntity::getId)
                                    .toList(),
                            ProcessingStatus.NEW
                    );

            tasks.forEach(task -> task.setStatus(ProcessingStatus.PROCESSING));

            Map<Long, List<TaskContext>> idsTasksMap = tasks
                    .stream()
                    .map(task -> {
                        return TaskContext.builder()
                                .taskId(task.getId())
                                .batchId(task.getBatchId())
                                .documentId(task.getDocumentId())
                                .status(task.getStatus())
                                .build();
                    })
                    .collect(Collectors.groupingBy(TaskContext::batchId));

            return butches.stream()
                    .map(batch -> {
                        return BatchContext.builder()
                                .batchId(batch.getId())
                                .operationId(batch.getOperationId())
                                .type(batch.getType())
                                .taskContexts(idsTasksMap.get(batch.getId()))
                                .build();
                    })
                    .collect(Collectors.toMap(
                            Function.identity(),
                            batch -> idsTasksMap.getOrDefault(batch.batchId(), List.of())
                    ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public void updateBatchAndTaskStatus(
            List<Long> batchIds,
            List<Long> taskIds,
            ProcessingStatus status
    ) {
        var updatedBatches = batchRepository.updateStatus(batchIds, status);

        if (updatedBatches == 0) {
            throw new IllegalStateException(
                    "Failed to update outbox job. batchIds=" + batchIds
            );
        }

        var updatedTasksCount = taskRepository.updateStatus(taskIds, status);

        if (updatedTasksCount != taskIds.size()) {
            throw new IllegalStateException(
                    "Expected to update " + taskIds.size() +
                            " outbox taskExecutionResults but updated " + updatedTasksCount
            );
        }
    }

    @Transactional
    @Override
    public void updateBatchAndTaskStatus(
            Map<Long, ProcessingStatus> batchStatusMap,
            Map<Long, ProcessingStatus> taskStatusMap
    ) {
        try {
            Map<ProcessingStatus, List<Long>> batchesGrouped = batchStatusMap.entrySet()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Map.Entry::getValue,
                            Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                    ));

            log.debug("Updating job statuses: {}", batchesGrouped);

            batchesGrouped.forEach((status, ids) -> {
                if (ids != null && !ids.isEmpty()) {
                    batchRepository.updateStatus(ids, status);
                }
            });

            Map<ProcessingStatus, List<Long>> tasksGrouped = taskStatusMap.entrySet()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Map.Entry::getValue,
                            Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                    ));

            tasksGrouped.forEach((status, ids) -> {
                if (ids != null && !ids.isEmpty()) {
                    taskRepository.updateStatus(ids, status);
                }
            });

            log.debug("Updated item statuses: {}", tasksGrouped);

            var doneBatchIds = batchStatusMap.entrySet().stream()
                    .filter(e -> e.getValue() == ProcessingStatus.DONE)
                    .map(Map.Entry::getKey)
                    .toList();

            if (!doneBatchIds.isEmpty()) {
                var doneBatches = batchRepository.findAllById(doneBatchIds);
                doneBatches.forEach(batch ->
                        processingOperationService
                                .onBatchCompleted(batch.getOperationId())
                );
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
