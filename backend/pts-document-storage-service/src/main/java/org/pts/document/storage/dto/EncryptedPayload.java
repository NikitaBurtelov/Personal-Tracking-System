package org.pts.document.storage.dto;

import lombok.Builder;

@Builder
public record EncryptedPayload(
        byte[] encryptedDataKey,
        byte[] iv
) {
}
