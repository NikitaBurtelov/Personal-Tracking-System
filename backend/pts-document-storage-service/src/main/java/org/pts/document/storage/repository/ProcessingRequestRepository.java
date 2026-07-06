package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.ProcessingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessingRequestRepository extends JpaRepository<ProcessingRequest, UUID> {
    @Modifying
    @Query(value = """
        UPDATE processing_request
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
}
