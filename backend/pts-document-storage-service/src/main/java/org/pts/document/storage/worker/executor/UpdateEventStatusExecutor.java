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
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateEventStatusExecutor {
    private final EventManager eventManager;
    private final ProcessingOperationManager processingOperationManager;

    @Transactional
    public void execute(List<BatchContext> batchContexts, List<UUID> eventIds) {
        try {
            eventManager.markEventsAsPublished(eventIds);

            batchContexts.forEach(context -> {
                context.setProcessingStatus(ProcessingStatus.DONE);
            });

            processingOperationManager.updateBatchStatus(batchContexts);

            log.info("Successfully processed publishing events, operationIds: {}",
                    eventIds
            );
        } catch (Exception e) {
            throw new RuntimeException("Error while processing publishing events", e);
        }
    }
}
