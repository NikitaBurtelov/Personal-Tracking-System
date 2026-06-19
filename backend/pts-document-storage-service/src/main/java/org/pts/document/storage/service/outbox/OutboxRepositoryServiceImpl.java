package org.pts.document.storage.service.outbox;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.OutboxJobEntity;
import org.pts.document.storage.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxRepositoryServiceImpl implements OutboxRepositoryService {
    private final OutboxRepository outboxRepository;

    @Override
    public void save(OutboxJobEntity entity) {
        outboxRepository.save(entity);
    }

    @Override
    public List<OutboxJobEntity> findByStatus(String value) {
        return outboxRepository.findAllByStatus(value);
    }

    @Transactional
    @Override
    public void setStatus(Long id, String value) {
        var entity = outboxRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("entity not found id: " + id));
        entity.setStatus(value);
    }

    @Transactional
    @Override
    public void setUpdatedAt(Long id, OffsetDateTime value) {
        var entity = outboxRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("entity not found id: " + id));
        entity.setUpdateAt(value);
    }
}
