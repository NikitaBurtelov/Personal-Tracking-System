package org.pts.document.storage.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.model.enums.DocumentStatus;

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
    @Column(name = "document_key", unique = false, updatable = true, nullable = true)
    private String key;
    @Column(name = "temp_key", unique = false, updatable = true, nullable = false)
    private String tempKey;
    @Column(name = "temp_bucket", unique = false, updatable = true, nullable = false)
    private String tempBucket;
    @Column(name = "encrypted_file_key", columnDefinition = "bytea", unique = false, updatable = true, nullable = true)
    private byte[] encryptedFileKey;
    @Column(name = "iv", columnDefinition = "bytea", unique = false, updatable = true, nullable = true)
    private byte[] iv;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, updatable = true, unique = false)
    private DocumentStatus status;
    @Column(name = "updated_at", nullable = false, updatable = true)
    private Instant updateAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();

        status = DocumentStatus.NEW;
        updateAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = Instant.now();
    }
}
