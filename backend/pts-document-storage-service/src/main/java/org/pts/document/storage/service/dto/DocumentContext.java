package org.pts.document.storage.service.dto;

import org.pts.document.storage.model.enums.DocumentStatus;

import java.util.UUID;

public record DocumentContext(
        UUID documentId,
        DocumentStatus status,
        String message
) {
}
