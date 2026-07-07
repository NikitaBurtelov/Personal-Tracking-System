package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.pts.document.storage.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public void markEventAsPublished(OutboxEventEntity event) {
        event.setPublished(true);
        outboxEventRepository.save(event);
    }

    @Transactional
    @Override
    public void markEventsAsPublished(List<OutboxEventEntity> events) {
        events.forEach(event -> event.setPublished(true));

        outboxEventRepository.saveAll(events);
    }
}
