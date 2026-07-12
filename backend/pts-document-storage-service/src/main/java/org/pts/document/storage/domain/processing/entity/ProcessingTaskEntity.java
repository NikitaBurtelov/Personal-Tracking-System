package org.pts.document.storage.domain.processing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.pts.document.storage.domain.enums.ProcessingStatus;

import java.util.UUID;

@Entity
@Table(name = "processing_task", schema = "document_storage_schema")
@Getter
@Setter
@Builder(builderMethodName = "builder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProcessingTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "batch_id")
    private Long batchId;
    @Column(name = "document_id")
    private UUID documentId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProcessingStatus status;
}
