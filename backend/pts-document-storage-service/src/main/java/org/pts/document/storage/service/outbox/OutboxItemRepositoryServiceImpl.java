package org.pts.document.storage.service.outbox;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.OutboxJobItemEntity;
import org.pts.document.storage.repository.OutboxItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OutboxItemRepositoryServiceImpl implements OutboxItemRepositoryService {
    private final OutboxItemRepository outboxItemRepository;

    @Override
    public void save(OutboxJobItemEntity entity) {
        outboxItemRepository.save(entity);
    }

    @Transactional
    @Override
    public List<OutboxJobItemEntity> findByJobId(Long id) {
        var jobs = outboxItemRepository.findAllByJobId(id);
        return jobs;
    }

    @Transactional
    @Override
    public void setStatus(Long id, String value) {
        var entity = outboxItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("entity not found id: " + id));
        entity.setStatus(value);
    }
}
