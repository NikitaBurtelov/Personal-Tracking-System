package org.pts.document.storage.domain.outbox.repository;

import org.pts.document.storage.domain.outbox.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(
            value = """
                    select *
                    from document_storage_schema.outbox_event
                    where published = false
                    order by created_at asc
                    limit :limit
                    for update skip locked
                    """,
            nativeQuery = true
    )
    List<OutboxEventEntity> findUnpublishedEvent(
            @Param("limit") int limit
    );

    @Query(
            value = """
                    select *
                    from document_storage_schema.outbox_event
                    where published = false and operation_id in :ids
                    for update skip locked
                    """,
            nativeQuery = true
    )
    List<OutboxEventEntity> findUnpublishedEvent(
            @Param("ids") List<UUID> ids
    );

    @Query("""
            select e.id
            from OutboxEventEntity e
            where e.operationId in :operationIds
            """)
    List<UUID> findEventIdsByOperationIdIn(Collection<UUID> operationIds);
}
