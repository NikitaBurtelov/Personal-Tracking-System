package org.pts.document.storage.service.dto;

import lombok.Builder;
import org.pts.document.storage.model.enums.ProcessingType;

import java.util.List;
import java.util.UUID;

@Builder
public record BatchContext(
        Long batchId,
        UUID operationId,
        ProcessingType type,
        List<TaskContext> taskContexts
) {
}
