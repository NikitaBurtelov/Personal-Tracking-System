package org.pts.document.storage.domain.context;

import java.util.UUID;

public record UploadResult(
        UUID docId,
        String result,
        String message
) {
}