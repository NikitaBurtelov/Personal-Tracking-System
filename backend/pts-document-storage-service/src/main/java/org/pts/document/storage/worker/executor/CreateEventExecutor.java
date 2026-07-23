package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.domain.context.BatchContext;
import org.pts.document.storage.domain.enums.ProcessingStatus;
import org.pts.document.storage.domain.outbox.EventManager;
import org.pts.document.storage.domain.processing.ProcessingOperationManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateEventExecutor {
    private final EventManager eventManager;
    private final ProcessingOperationManager processingOperationManager;

    @Transactional
    public void execute(List<BatchContext> batchContexts) {
        if (batchContexts == null || batchContexts.isEmpty()) {
            return;
        }

        try {
            var operationIds = batchContexts.stream()
                    .map(batchContext -> {
                        batchContext.setProcessingStatus(ProcessingStatus.CREATED_EVENT);
                        return batchContext.getOperationId();
                    })
                    .collect(Collectors.toSet());

            eventManager.createEvent(operationIds);

            processingOperationManager.updateBatchStatus(batchContexts);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
