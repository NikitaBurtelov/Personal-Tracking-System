package org.pts.document.storage.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record JobItemExecutionResult(
        Long itemId,
        UUID documentId,
        String result,
        String message
) {
}
