package org.pts.document.storage.domain.context;

import org.pts.document.storage.domain.enums.DocumentStatus;

import java.util.UUID;

public record DocumentContext(
        UUID documentId,
        DocumentStatus status,
        String message
) {
}
