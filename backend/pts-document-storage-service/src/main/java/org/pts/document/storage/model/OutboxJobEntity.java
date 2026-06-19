package org.pts.document.storage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

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
    private String type;
    @Column(name = "status", unique = false, nullable = false)
    private String status; //NEW | PROCESSING | DONE | FAILED
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false, updatable = false)
    private OffsetDateTime updateAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        status = "NEW";
        updateAt = createdAt;
    }
}