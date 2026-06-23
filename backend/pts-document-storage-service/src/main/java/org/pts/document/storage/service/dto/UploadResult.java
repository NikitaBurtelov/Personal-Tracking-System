package org.pts.document.storage.service.dto;

import java.util.UUID;

public record UploadResult(
        UUID docId,
        String result,
        String message
) {
}