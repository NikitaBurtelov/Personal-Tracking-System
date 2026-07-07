package org.pts.document.storage.service.outbox;

import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EventManagerService {
    @Transactional
    List<OutboxEventEntity> getUnpublishedEvents(int limit);

    @Transactional
    void markEventAsPublished(OutboxEventEntity event);

    @Transactional
    void markEventsAsPublished(List<OutboxEventEntity> events);
}
