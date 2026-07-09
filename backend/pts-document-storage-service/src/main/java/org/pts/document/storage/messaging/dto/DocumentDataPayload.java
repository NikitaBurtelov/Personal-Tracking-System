package org.pts.document.storage.messaging.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DocumentDataPayload(
        UUID workId,
        String bucket,
        List<DocumentData> documentData
) {
    @Builder
    public record DocumentData(
            String s3Key,
            UUID id
    ) {
    }
}
