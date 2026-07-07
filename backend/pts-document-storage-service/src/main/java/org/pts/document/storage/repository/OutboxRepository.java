package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

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
            @Param("status") Status status
    );

    @Modifying
    @Query("""
                UPDATE OutboxJobEntity j
                SET j.status = :status
                WHERE j.id IN :jobsId
            """)
    int updateStatus(
            @Param("jobsId") List<Long> jobsId,
            @Param("status") Status status
    );

    List<OutboxJobEntity> findAllByRequestId(UUID requestId);
}