package org.pts.document.storage.domain.outbox;

import org.pts.document.storage.domain.outbox.entity.OutboxEventEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface EventManager {
    @Transactional
    List<OutboxEventEntity> getUnpublishedEvents(int limit);

    @Transactional
    List<OutboxEventEntity> getUnpublishedEvents(List<UUID> eventIds);

    @Transactional
    void createEvent(Set<UUID> operationIds);

    @Transactional
    void markEventsAsPublished(List<UUID> eventsIds);
}
