package org.pts.document.storage.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.model.enums.Status;

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

    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Column(columnDefinition = "jsonb", nullable = true)
    private String payload;

    private Status status;

    private boolean published;

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
        payload = null;
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
