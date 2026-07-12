package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.domain.context.BatchContext;
import org.pts.document.storage.domain.context.DocumentContext;
import org.pts.document.storage.domain.context.TaskContext;
import org.pts.document.storage.domain.document.DocumentManager;
import org.pts.document.storage.domain.enums.ProcessingStatus;
import org.pts.document.storage.domain.processing.ProcessingOperationManager;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetDocumentExecutor {
    private final DocumentManager documentManager;
    private final ProcessingOperationManager processingOperationManager;

    public Map<UUID, DocumentContext> execute(List<BatchContext> batchContexts) {
        if (batchContexts.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return batchContexts.stream()
                    .flatMap(entry ->
                            documentManager.fetchDocumentsAsync(
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

            log.error("Failed to get documents for tasks: {} ", batchIds, e);

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
