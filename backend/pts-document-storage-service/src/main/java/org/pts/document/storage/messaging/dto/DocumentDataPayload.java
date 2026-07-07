package org.pts.document.storage.messaging.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DocumentDataPayload(
        String bucket,
        List<String> temps3Keys
) {
}
