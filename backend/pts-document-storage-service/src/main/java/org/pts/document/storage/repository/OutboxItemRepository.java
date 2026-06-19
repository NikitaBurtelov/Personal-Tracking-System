package org.pts.document.storage.repository;

import org.pts.document.storage.model.OutboxJobItemEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OutboxItemRepository extends CrudRepository<OutboxJobItemEntity, Long> {
    List<OutboxJobItemEntity> findAllByStatus(String status);

    List<OutboxJobItemEntity> findAllByJobId(Long jobId);
}
