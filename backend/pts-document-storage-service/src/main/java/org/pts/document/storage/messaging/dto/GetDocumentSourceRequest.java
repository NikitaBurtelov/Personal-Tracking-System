package org.pts.document.storage.messaging.dto;

import java.util.List;
import java.util.UUID;

public record GetDocumentSourceRequest(
        UUID workId,
        List<String> s3Keys
) { }

