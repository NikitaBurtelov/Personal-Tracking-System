package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.pts.document.storage.service.document.DocumentManagerService;
import org.pts.document.storage.service.dto.BatchContext;
import org.pts.document.storage.service.dto.DocumentContext;
import org.pts.document.storage.service.dto.TaskContext;
import org.pts.document.storage.service.processing.ProcessingOperationManager;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UploadDocumentExecutor {
    private final DocumentManagerService documentManagerService;
    private final ProcessingOperationManager processingOperationManager;

    public Map<UUID, DocumentContext> execute(List<BatchContext> batchContexts) {
        try {
            return batchContexts.stream()
                    .flatMap(entry ->
                            documentManagerService.uploadDocumentsAsync(
                                            entry.getTaskContexts().stream()
                                                    .map(TaskContext::getDocumentId)
                                                    .toList()
                                    )
                                    .stream()
                    )
                    .collect(Collectors.toMap(
                            DocumentContext::documentId,
                            Function.identity(),
                            (oldValue, newValue) -> oldValue
                    ));
        } catch (Exception e) {
            var batchIds = batchContexts.stream()
                    .map(BatchContext::getBatchId)
                    .toList();
            var taskIds = batchContexts.stream()
                    .flatMap(task -> task.getTaskContexts().stream())
                    .map(TaskContext::getTaskId)
                    .toList();

            markFailed(
                    batchIds,
                    taskIds
            );

            log.error("Failed to upload documents for tasks: {} ", batchIds, e);

            return Collections.emptyMap();
        }
    }

    private void markFailed(List<Long> batchIds, List<Long> taskIds) {
        processingOperationManager.updateBatchAndTaskStatus(
                batchIds,
                taskIds,
                ProcessingStatus.FAILED
        );
    }
}

