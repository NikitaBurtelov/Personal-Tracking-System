package org.pts.document.storage.domain.outbox.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.domain.enums.EventStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event", schema = "document_storage_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventEntity {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "operation_id", nullable = false, updatable = false)
    private UUID operationId;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private boolean published;

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        status = EventStatus.NEW;
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
