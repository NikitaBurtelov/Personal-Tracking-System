package org.pts.document.storage.service.outbox;

import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OutboxItemRepositoryService {
    void save(OutboxJobItemEntity entity);

    @Transactional
    List<OutboxJobItemEntity> findByJobId(Long id);

    @Transactional
    void setStatus(Long id, String value);
}
