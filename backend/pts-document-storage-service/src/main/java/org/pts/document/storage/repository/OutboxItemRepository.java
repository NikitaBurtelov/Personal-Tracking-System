package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.JobStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface OutboxItemRepository extends CrudRepository<OutboxJobItemEntity, Long> {

    List<OutboxJobItemEntity> findAllByJobIdIn(Collection<Long> jobIds);

    @Modifying
    @Query("""
            UPDATE OutboxJobItemEntity i
            SET i.status = :status
            WHERE i.id IN :itemIds
            """)
    int updateStatus(List<Long> itemIds, JobStatus status);

    List<OutboxJobItemEntity> findAllByJobIdInAndStatus(Collection<Long> jobIds, JobStatus status);
}
