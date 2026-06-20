package org.pts.document.storage.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.model.enums.OutboxJobStatus;
import org.pts.document.storage.model.enums.OutboxJobType;

import java.time.Instant;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@Builder(builderMethodName = "builder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OutboxJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "type", unique = false, nullable = false)
    private OutboxJobType type;
    @Column(name = "status", unique = false, nullable = false)
    private OutboxJobStatus status; //NEW | PROCESSING | DONE | FAILED
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false, updatable = false)
    private Instant updateAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        status = OutboxJobStatus.NEW;
        updateAt = createdAt;
    }
}