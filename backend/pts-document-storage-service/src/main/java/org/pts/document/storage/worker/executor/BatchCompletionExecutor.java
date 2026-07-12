package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.service.dto.BatchContext;
import org.pts.document.storage.service.processing.ProcessingOperationManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchCompletionExecutor {
    private final ProcessingOperationManager processingOperationManager;

    public void execute(List<BatchContext> batchContexts) {
        if (batchContexts != null && !batchContexts.isEmpty()) {
            processingOperationManager.onBatchCompleted(batchContexts);
        }
    }
}
