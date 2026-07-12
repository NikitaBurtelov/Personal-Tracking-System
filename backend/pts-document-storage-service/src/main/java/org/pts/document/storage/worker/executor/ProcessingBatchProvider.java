package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.domain.context.BatchContext;
import org.pts.document.storage.domain.enums.ProcessingStatus;
import org.pts.document.storage.domain.enums.ProcessingType;
import org.pts.document.storage.domain.processing.ProcessingOperationManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProcessingBatchProvider {
    private final ProcessingOperationManager processingOperationManager;

    public Map<ProcessingStatus, List<BatchContext>> take(
            ProcessingType processingType,
            int limit
    ) {
        return processingOperationManager.takeForProcessing(
                processingType,
                limit
        );
    }
}
