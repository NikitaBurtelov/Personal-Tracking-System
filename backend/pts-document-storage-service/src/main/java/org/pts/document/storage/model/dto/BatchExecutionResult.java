package org.pts.document.storage.model.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record BatchExecutionResult(
        Long batchId,
        UUID operationId,
        List<TaskExecutionResult> taskExecutionResults
) {
}