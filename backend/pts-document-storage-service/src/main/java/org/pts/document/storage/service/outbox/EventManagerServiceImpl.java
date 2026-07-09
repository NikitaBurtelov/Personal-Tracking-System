package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.pts.document.storage.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventManagerServiceImpl implements EventManagerService {
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    @Override
    public List<OutboxEventEntity> getUnpublishedEvents(int limit) {
        return outboxEventRepository.findUnpublishedBatch(limit);
    }

    @Transactional
    @Override
    public List<OutboxEventEntity> getUnpublishedEvents(List<UUID> eventIds) {
        return outboxEventRepository.findUnpublishedBatch(eventIds);
    }

    @Transactional
    @Override
    public void markEventsAsPublished(List<UUID> eventsIds) {
        var events = outboxEventRepository.findAllById(eventsIds);

        if (events.isEmpty()) {
            return;
        }

        events.forEach(event -> event.setPublished(true));

        outboxEventRepository.saveAll(events);
    }
}
