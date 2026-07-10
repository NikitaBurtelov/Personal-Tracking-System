package org.pts.document.storage.model.dto;

import lombok.Builder;

@Builder
public record DocumentEncryptedPayload(
        byte[] encryptedDataKey,
        byte[] iv
) {
}
