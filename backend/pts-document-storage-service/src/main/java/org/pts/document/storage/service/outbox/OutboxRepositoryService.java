package org.pts.document.storage.service.outbox;

import org.pts.document.storage.model.OutboxJobEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

public interface OutboxRepositoryService {
    void save(OutboxJobEntity entity);

    List<OutboxJobEntity> findByStatus(String value);

    @Transactional
    void setStatus(Long id, String value);

    @Transactional
    void setUpdatedAt(Long id, OffsetDateTime value);
}
