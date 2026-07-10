package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.repository.OutboxEventRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OperationEventsFinder {
    private final OutboxEventRepository outboxEventRepository;

    public List<UUID> findEventByCompletedOperation(List<UUID> operationIds) {
        var eventsIds = outboxEventRepository.findEventIdsByOperationIdIn(operationIds);

        if (eventsIds.isEmpty()) {
            return Collections.emptyList();
        }

        return eventsIds;
    }
}
