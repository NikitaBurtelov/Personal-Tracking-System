package org.pts.document.storage.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.pts.document.storage.model.enums.ProcessingType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processing_operation", schema = "document_storage_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingOperation {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ProcessingType type;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;

    @Column(name = "total_batch")
    private int totalBatch;

    @Column(name = "completed_batch")
    private int completedBatch;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
        updatedAt = createdAt;
        completedBatch = 0;
        status = ProcessingStatus.NEW;
    }
}
