package org.pts.document.storage.domain.document.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.domain.enums.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document", schema = "document_storage_schema")
@Getter
@Setter
@Builder(builderMethodName = "builder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DocumentEntity {
    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "object_key", unique = false, updatable = true, nullable = true)
    private String objectKey;
    @Column(name = "transfer_object_key", unique = false, updatable = true, nullable = false)
    private String transferObjectKey;
    @Column(name = "transfer_bucket", unique = false, updatable = true, nullable = false)
    private String transferBucket;
    @Column(name = "encrypted_data_key", columnDefinition = "bytea", unique = false, updatable = true, nullable = true)
    private byte[] encryptedDataKey;
    @Column(name = "initialization_vector", columnDefinition = "bytea", unique = false, updatable = true, nullable = true)
    private byte[] initializationVector;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, updatable = true, unique = false)
    private DocumentStatus status;
    @Column(name = "updated_at", nullable = false, updatable = true)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();

        status = DocumentStatus.NEW;
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
