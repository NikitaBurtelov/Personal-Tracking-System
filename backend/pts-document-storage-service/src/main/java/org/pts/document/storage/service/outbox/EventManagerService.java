package org.pts.document.storage.service.outbox;

import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface EventManagerService {
    @Transactional
    List<OutboxEventEntity> getUnpublishedEvents(int limit);

    @Transactional
    List<OutboxEventEntity> getUnpublishedEvents(List<UUID> eventIds);

    @Transactional
    void markEventsAsPublished(List<UUID> eventsIds);
}
