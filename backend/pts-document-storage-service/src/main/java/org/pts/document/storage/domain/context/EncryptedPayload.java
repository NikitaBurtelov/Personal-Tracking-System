package org.pts.document.storage.domain.context;

import lombok.Builder;

@Builder
public record EncryptedPayload(
        byte[] encryptedDataKey,
        byte[] iv
) {
}
