package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.BatchExecutionResult;
import org.pts.document.storage.model.dto.TaskExecutionResult;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.pts.document.storage.model.enums.ProcessingType;
import org.pts.document.storage.service.document.DocumentManagerService;
import org.pts.document.storage.service.dto.BatchContext;
import org.pts.document.storage.service.dto.DocumentContext;
import org.pts.document.storage.service.dto.TaskContext;
import org.pts.document.storage.service.processing.ProcessingOperationManager;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetDocumentExecutor {
    private final DocumentManagerService documentManagerService;
    private final ProcessingOperationManager processingOperationManager;

    public List<BatchExecutionResult> execute() {
        var batches = processingOperationManager.takeForProcessing(
                ProcessingType.GET,
                ProcessingStatus.NEW,
                10
        );

        if (batches == null || batches.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return batches.entrySet()
                    .stream()
                    .map(entry -> {
                        var tasks = entry.getValue();

                        var documents = documentManagerService.fetchDocumentsAsync(
                                        tasks.stream()
                                                .map(TaskContext::documentId)
                                                .toList()
                                ).stream()
                                .collect(Collectors.toMap(
                                        DocumentContext::documentId,
                                        Function.identity()
                                ));

                        return BatchExecutionResult.builder()
                                .operationId(entry.getKey().operationId())
                                .batchId(entry.getKey().batchId())
                                .taskExecutionResults(tasks.stream()
                                        .map(item -> {
                                            var document = documents.get(item.documentId());

                                            return TaskExecutionResult.builder()
                                                    .taskId(item.taskId())
                                                    .documentId(document.documentId())
                                                    .result(document.result())
                                                    .message(document.message())
                                                    .build();
                                        })
                                        .toList())
                                .build();
                    })
                    .toList();
        } catch (Exception e) {
            var batchIds = batches.keySet()
                    .stream()
                    .map(BatchContext::batchId)
                    .toList();
            var taskIds = batches.values()
                    .stream()
                    .flatMap(List::stream)
                    .map(TaskContext::taskId)
                    .toList();

            log.error("Failed to upload documents for tasks: {} ", batchIds, e);

            markFailed(
                    batchIds,
                    taskIds
            );

            return Collections.emptyList();
        }
    }


    private void markFailed(List<Long> batchIds, List<Long> taskIds) {
        processingOperationManager.updateBatchAndTaskStatus(
                batchIds,
                taskIds,
                ProcessingStatus.FAILED
        );
    }
//        jobs.forEach((job, taskExecutionResults) -> {
//            try {
//                var docs = taskExecutionResults.stream().map(OutboxJobItemEntity::getDocumentId).toList();
//
//                var itemsDocsMap = taskExecutionResults.stream()
//                        .collect(
//                                Collectors.toMap(
//                                        OutboxJobItemEntity::getDocumentId,
//                                        item -> item)
//                        );
//
//                var results = documentManagerService.fetchDocumentsAsync(docs);
//
//                jobsData.add(
//                        JobResult.builder()
//                                .job(job)
//                                .taskExecutionResults(taskExecutionResults)
//                                .results(results)
//                                .build());
//
//                identifyAndUpdateStatus(job, itemsDocsMap, results);
//
//            } catch (Exception e) {
//                markFailed(job, taskExecutionResults);
//            }
//        });

}
