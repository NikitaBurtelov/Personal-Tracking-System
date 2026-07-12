package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.repository.OutboxEventRepository;
import org.pts.document.storage.service.dto.BatchContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OperationEventsFinder {
    //TODO переделать на EventManager
    private final OutboxEventRepository outboxEventRepository;

//    public List<UUID> findEventByCompletedOperation(List<UUID> operationIds) {
//        var eventsIds = outboxEventRepository.findEventIdsByOperationIdIn(operationIds);
//
//        if (eventsIds.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        return eventsIds;
//    }

    public List<UUID> findEventByCompletedOperation(List<BatchContext> batchContexts) {
        if (batchContexts == null || batchContexts.isEmpty()) {
            return Collections.emptyList();
        }

        var eventsIds = outboxEventRepository.findEventIdsByOperationIdIn(
                batchContexts.stream()
                        .map(BatchContext::getOperationId)
                        .toList()
        );

        if (eventsIds.isEmpty()) {
            return Collections.emptyList();
        }

        return eventsIds;
    }
}
