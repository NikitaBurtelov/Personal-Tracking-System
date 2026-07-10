package org.pts.document.storage.service.dto;

import lombok.Builder;
import org.pts.document.storage.model.enums.ProcessingStatus;

import java.util.UUID;

@Builder
public record TaskContext(
        Long taskId,
        UUID documentId,
        Long batchId,
        ProcessingStatus status
) {
}