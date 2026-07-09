package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.ProcessingOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProcessingOperationRepository extends JpaRepository<ProcessingOperation, UUID> {

    @Query(value = """
            UPDATE document_storage_schema.processing_operation
            SET
                completed_jobs = completed_jobs + 1,
                status = CASE
                            WHEN completed_jobs + 1 = total_jobs
                            THEN 'DONE'
                            ELSE status
                         END,
                completed_at = CASE
                            WHEN completed_jobs + 1 = total_jobs
                            THEN now()
                            ELSE completed_at
                         END
            WHERE id = :operationId
              AND status = 'PROCESSING'
            RETURNING status
            """, nativeQuery = true)
    String completeJob(UUID operationId);

    @Query(value = """
            SELECT *
            FROM document_storage_schema.processing_operation
            WHERE id in :ids
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<ProcessingOperation> findAllByIdIn(
            @Param("ids") Collection<UUID> ids
    );
}