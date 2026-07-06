package org.pts.document.storage.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.model.enums.RequestStatus;
import org.pts.document.storage.model.enums.RequestType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processing_request", schema = "document_storage_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingRequest {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private RequestType type;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(name = "total_jobs")
    private int totalJobs;

    @Column(name = "completed_jobs")
    private int completedJobs;

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
        completedJobs = 0;
        status = RequestStatus.NEW;
    }

    public void markJobCompleted() {
        this.completedJobs++;

        if (this.completedJobs == this.totalJobs) {
            this.status = RequestStatus.DONE;
            this.completedAt = Instant.now();
            this.updatedAt = this.completedAt;
        }
    }

    public void markFailed() {
        this.status = RequestStatus.FAILED;
        this.completedAt = Instant.now();
    }

    public void start(int totalJobs, int totalItems) {
        this.status = RequestStatus.PROCESSING;
        this.totalJobs = totalJobs;
        this.completedJobs = 0;
    }
}
