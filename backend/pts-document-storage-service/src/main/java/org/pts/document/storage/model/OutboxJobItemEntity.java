package org.pts.document.storage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "outbox_item")
@Getter
@Setter
@Builder(builderMethodName = "builder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OutboxJobItemEntity {
    @Id
    private Long id;
    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "document_id")
    private UUID documentId;
    @Column(name = "status")
    private String status; // NEW, PROCESSING, DONE, FAILED
}
