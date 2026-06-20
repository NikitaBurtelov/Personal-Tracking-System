package org.pts.document.storage.repository;

import jakarta.persistence.LockModeType;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.enums.OutboxJobStatus;
import org.pts.document.storage.model.enums.OutboxJobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxJobEntity, Long> {


    @Modifying
    @Query(value = """
                UPDATE outbox
                SET status = 'PROCESSING'
                WHERE id IN (
                    SELECT id
                    FROM outbox
                    WHERE status = :status
                    ORDER BY id
                    LIMIT :limit
                )
                RETURNING *
            """)
    List<OutboxJobEntity> claimBatch(
            @Param("status") String status,
            @Param("limit") int limit
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Modifying
    @Query(value = """
            UPDATE outbox
            SET status = 'PROCESSING'
            WHERE id IN (
                SELECT id
                FROM outbox
                WHERE status = :status
                ORDER BY id
            )
            RETURNING *
            """)
    List<OutboxJobEntity> findAllByStatus(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxJobEntity> findAllByTypeAndStatus(OutboxJobType type, OutboxJobStatus status);

    OutboxJobEntity findAllById(Long id);

    @Modifying
    @Query("""
            UPDATE OutboxJobEntity j
            SET j.status = :status
            WHERE j.id = :jobId
            """)
    int updateStatus(Long jobId, OutboxJobStatus status);
}
