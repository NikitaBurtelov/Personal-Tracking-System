package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.ProcessingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProcessingRequestRepository extends JpaRepository<ProcessingRequest, UUID> {

    @Query(value = """
            UPDATE document_storage_schema.processing_request
            SET
                completed_jobs = completed_jobs + 1,
                status = CASE
                            WHEN completed_jobs + 1 = total_jobs
                            THEN 'COMPLETED'
                            ELSE status
                         END,
                completed_at = CASE
                            WHEN completed_jobs + 1 = total_jobs
                            THEN now()
                            ELSE completed_at
                         END
            WHERE id = :requestId
              AND status = 'PROCESSING'
            RETURNING status
            """, nativeQuery = true)
    String completeJob(UUID requestId);

    @Query(value = """
            SELECT *
            FROM document_storage_schema.processing_request
            WHERE id in :ids
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<ProcessingRequest> findAllByIdIn(
            @Param("ids") Collection<UUID> ids
    );
}