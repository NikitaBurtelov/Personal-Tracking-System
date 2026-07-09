package org.pts.document.storage.service.dto;

import lombok.Builder;

@Builder
public record EncryptedPayload(
        byte[] encryptedDataKey,
        byte[] iv
) {
}
