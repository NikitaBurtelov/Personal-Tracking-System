package org.pts.document.storage.messaging.dto;

import java.util.List;
import java.util.UUID;

public record GetDocumentSourceResponse(
        UUID workId,
        String bucket,
        List<String> temps3Keys
) {
}
