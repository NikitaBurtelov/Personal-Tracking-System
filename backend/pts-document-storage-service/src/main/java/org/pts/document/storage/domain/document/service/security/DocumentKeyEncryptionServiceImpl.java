package org.pts.document.storage.domain.document.service.security;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.config.security.VaultTransitProperties;
import org.pts.document.storage.vault.VaultTransitClient;
import org.pts.document.storage.vault.exception.VaultTransitException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentKeyEncryptionServiceImpl implements DocumentKeyEncryptionService {
    private final VaultTransitProperties properties;

    private final VaultTransitClient vaultTransitClient;

    @Override
    public String encryptDocumentKey(byte[] data) {
        try {
            return vaultTransitClient.encrypt(
                    documentEncryptionKey(),
                    data
            );

        } catch (RuntimeException e) {
            throw new VaultTransitException(
                    "Failed to encrypt document key",
                    e
            );
        }
    }

    @Override
    public byte[] decryptDocumentKey(String cipherData) {
        try {
            return vaultTransitClient.decrypt(
                    documentEncryptionKey(),
                    cipherData
            );
        } catch (RuntimeException e) {
            throw new VaultTransitException(
                    "Failed to decrypt document key",
                    e
            );
        }
    }

    private String documentEncryptionKey() {
        return properties
                .getDocumentEncryption()
                .getKey();
    }
}