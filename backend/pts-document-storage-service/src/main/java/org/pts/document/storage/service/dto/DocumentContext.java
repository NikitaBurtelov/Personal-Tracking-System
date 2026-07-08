package org.pts.document.storage.service.dto;

import java.util.UUID;

public record DocumentContext(
        UUID documentId,
        String result,
        String message
) {
}
