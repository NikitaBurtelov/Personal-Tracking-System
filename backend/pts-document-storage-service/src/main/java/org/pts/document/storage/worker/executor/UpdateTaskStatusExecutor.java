package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.domain.enums.DocumentStatus;
import org.pts.document.storage.domain.enums.ProcessingStatus;
import org.pts.document.storage.domain.context.BatchContext;
import org.pts.document.storage.domain.context.DocumentContext;
import org.pts.document.storage.domain.processing.ProcessingOperationManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateTaskStatusExecutor {
    private final ProcessingOperationManager processingOperationManager;

    public void execute(
            List<BatchContext> batchContexts,
            Map<UUID, DocumentContext> documentContextMap
    ) {
        if (documentContextMap == null || documentContextMap.isEmpty()) {
            return;
        }

        try {
            batchContexts.forEach(batchContext -> {
                AtomicReference<ProcessingStatus> batchStatus = new AtomicReference<>(ProcessingStatus.DOCUMENTS_UPLOADED);

                batchContext.getTaskContexts().forEach(taskContext -> {
                    if (documentContextMap.containsKey(taskContext.getDocumentId())) {
                        var status = documentContextMap.get(taskContext.getDocumentId()).status();

                        if (status == DocumentStatus.UPLOADED) {
                            taskContext.setProcessingStatus(
                                    ProcessingStatus.DOCUMENTS_UPLOADED
                            );
                        } else if (status == DocumentStatus.FAILED) {
                            taskContext.setProcessingStatus(
                                    ProcessingStatus.FAILED
                            );

                            batchStatus.set(ProcessingStatus.PROCESSING); //TODO подумать
                        }
                    }
                });

                batchContext.setProcessingStatus(batchStatus.get());
            });

            processingOperationManager.updateBatchAndTaskStatus(
                    batchContexts
            );
        } catch (Exception e) {
            log.error("Failed to update statuses", e);
        }
    }
}
