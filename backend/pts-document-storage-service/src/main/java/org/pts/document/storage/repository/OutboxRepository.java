package org.pts.document.storage.repository;

import jakarta.persistence.LockModeType;
import org.pts.document.storage.model.OutboxJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    """, nativeQuery = true)
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
    """, nativeQuery = true)
    List<OutboxJobEntity> findAllByStatus(String status);
}
