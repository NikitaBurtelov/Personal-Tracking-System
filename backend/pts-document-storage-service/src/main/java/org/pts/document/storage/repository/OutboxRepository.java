package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.enums.OutboxJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxJobEntity, Long> {
    @Query(value = """
            SELECT *
            FROM outbox
            WHERE type = :type
              AND status = :status
            ORDER BY id
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, nativeQuery = true)
    List<OutboxJobEntity> findAllByTypeAndStatus(
            @Param("type") String type,
            @Param("status") String status,
            @Param("limit") int limit
    );

    @Modifying
    @Query("""
                UPDATE OutboxJobEntity j
                SET j.status = :status
                WHERE j.id = :jobId
            """)
    int updateStatus(
            @Param("jobId") Long jobId,
            @Param("status") OutboxJobStatus status
    );
}
