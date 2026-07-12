package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.pts.document.storage.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventManagerImpl implements EventManager {
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    @Override
    public List<OutboxEventEntity> getUnpublishedEvents(int limit) {
        return outboxEventRepository.findUnpublishedEvent(limit);
    }

    @Transactional
    @Override
    public List<OutboxEventEntity> getUnpublishedEvents(List<UUID> eventIds) {
        return outboxEventRepository.findUnpublishedEvent(eventIds);
    }

    @Transactional
    @Override
    public void createEvent(Set<UUID> operationIds) {
        var events = new ArrayList<OutboxEventEntity>(operationIds.size());

        operationIds.forEach(operationId -> {
            var event = OutboxEventEntity.builder()
                    .operationId(operationId)
                    .published(false)
                    .build();

            events.add(event);
        });

        outboxEventRepository.saveAll(events);
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
