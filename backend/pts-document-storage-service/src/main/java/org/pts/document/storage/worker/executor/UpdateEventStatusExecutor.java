package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.service.outbox.EventManagerService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateEventStatusExecutor {
    private final EventManagerService eventManagerService;

    public void execute(List<UUID> eventIds) {
        try {
            eventManagerService.markEventsAsPublished(eventIds);

            log.info("Successfully processed publishing events, operationIds: {}",
                    eventIds
            );
        } catch (Exception e) {
            throw new RuntimeException("Error while processing publishing events", e);
        }
    }
}
