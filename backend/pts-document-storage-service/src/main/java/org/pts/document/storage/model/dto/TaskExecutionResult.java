package org.pts.document.storage.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TaskExecutionResult(
        Long taskId,
        UUID documentId,
        String result,
        String message
) {
}
