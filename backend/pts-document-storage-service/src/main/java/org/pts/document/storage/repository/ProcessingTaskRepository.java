package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.ProcessingTaskEntity;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface ProcessingTaskRepository extends CrudRepository<ProcessingTaskEntity, Long> {

    List<ProcessingTaskEntity> findAllByBatchIdIn(Collection<Long> batchIds);

    @Modifying
    @Query("""
            UPDATE ProcessingTaskEntity i
            SET i.status = :status
            WHERE i.id IN :itemIds
            """)
    int updateStatus(List<Long> itemIds, ProcessingStatus status);

    List<ProcessingTaskEntity> findAllByBatchIdInAndStatus(Collection<Long> batchIds, ProcessingStatus status);
}
