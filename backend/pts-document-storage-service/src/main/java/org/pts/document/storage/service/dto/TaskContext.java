package org.pts.document.storage.service.dto;

import lombok.Builder;
import lombok.Data;
import org.pts.document.storage.model.enums.ProcessingStatus;

import java.util.UUID;

@Data
@Builder
public class TaskContext {
    private Long taskId;
    private UUID documentId;
    private Long batchId;
    private ProcessingStatus processingStatus;
}