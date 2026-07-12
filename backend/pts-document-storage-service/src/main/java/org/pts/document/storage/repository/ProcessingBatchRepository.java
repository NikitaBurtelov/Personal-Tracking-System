package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.ProcessingBatchEntity;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProcessingBatchRepository extends JpaRepository<ProcessingBatchEntity, Long> {
    @Query(value = """
            SELECT *
            FROM document_storage_schema.processing_batch
            WHERE type = :type
              AND status = :status
            ORDER BY id
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, nativeQuery = true)
    List<ProcessingBatchEntity> findAllByTypeAndStatus(
            @Param("type") String type,
            @Param("status") String status,
            @Param("limit") int limit
    );

    @Query(value = """
            UPDATE document_storage_schema.processing_batch
                        SET locked_until = NOW() + INTERVAL '5 minutes'
                        WHERE id IN (
                            SELECT id
                            FROM document_storage_schema.processing_batch
                            WHERE type = :type
                              AND status != 'DONE'
                              AND (
                                  locked_until IS NULL
                                  OR locked_until < NOW()
                              )
                            ORDER BY id
                            FOR UPDATE SKIP LOCKED
                            LIMIT :limit
                        )
                        RETURNING *;
            """, nativeQuery = true)
    List<ProcessingBatchEntity> findProcessingBatchByType(
            @Param("type") String type,
            @Param("limit") int limit
    );

    @Modifying
    @Query("""
                UPDATE ProcessingBatchEntity b
                SET b.status = :status
                WHERE b.id = :batchId
            """)
    int updateStatus(
            @Param("batchId") Long batchId,
            @Param("status") ProcessingStatus status
    );

    @Modifying
    @Query("""
                UPDATE ProcessingBatchEntity b
                SET b.status = :status
                WHERE b.id IN :batchIds
            """)
    int updateStatus(
            @Param("batchIds") List<Long> batchIds,
            @Param("status") ProcessingStatus status
    );

    List<ProcessingBatchEntity> findAllByOperationId(UUID operationId);
}