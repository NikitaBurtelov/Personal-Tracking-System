package org.pts.document.storage.domain.processing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.domain.enums.ProcessingStatus;
import org.pts.document.storage.domain.enums.ProcessingType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processing_batch", schema = "document_storage_schema")
@Getter
@Setter
@Builder(builderMethodName = "builder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProcessingBatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "operation_id", columnDefinition = "uuid", unique = false, nullable = false)
    private UUID operationId;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", unique = false, nullable = false)
    private ProcessingType type;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", unique = false, nullable = false)
    private ProcessingStatus status;
    @Column(name = "locked_until", unique = false, nullable = true)
    private Instant lockedUntil;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        status = ProcessingStatus.NEW;
        updatedAt = createdAt;
    }
}