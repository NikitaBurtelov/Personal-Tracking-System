package org.pts.document.storage.domain.processing.repository;

import org.pts.document.storage.domain.enums.ProcessingStatus;
import org.pts.document.storage.domain.processing.entity.ProcessingTaskEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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

    @Query(value = """
            SELECT *
            FROM document_storage_schema.processing_task
            WHERE batch_id IN :batchIds
              AND status != 'DONE'
            ORDER BY id
            """, nativeQuery = true)
    List<ProcessingTaskEntity> findProcessingTaskByBatchIds(
            @Param("batchIds") Collection<Long> batchIds
    );

    List<ProcessingTaskEntity> findAllByBatchIdInAndStatus(Collection<Long> batchIds, ProcessingStatus status);
}
