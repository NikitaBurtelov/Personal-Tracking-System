package org.pts.document.storage.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.model.enums.OutboxJobStatus;

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
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OutboxJobStatus status;
}
