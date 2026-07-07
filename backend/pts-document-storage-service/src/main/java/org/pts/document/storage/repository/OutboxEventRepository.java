package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(
            value = """
                    select *
                    from outbox_event
                    where published = false
                    order by created_at asc
                    limit :limit
                    for update skip locked
                    """,
            nativeQuery = true
    )
    List<OutboxEventEntity> findUnpublishedBatch(
            @Param("limit") int limit
    );
}
