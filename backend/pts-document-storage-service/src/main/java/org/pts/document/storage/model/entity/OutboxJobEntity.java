package org.pts.document.storage.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.model.enums.Status;
import org.pts.document.storage.model.enums.Type;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox", schema = "document_storage_schema")
@Getter
@Setter
@Builder(builderMethodName = "builder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OutboxJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "request_id", columnDefinition = "uuid", unique = false, nullable = false)
    private UUID requestId;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", unique = false, nullable = false)
    private Type type;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", unique = false, nullable = false)
    private Status status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updateAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        status = Status.NEW;
        updateAt = createdAt;
    }
}