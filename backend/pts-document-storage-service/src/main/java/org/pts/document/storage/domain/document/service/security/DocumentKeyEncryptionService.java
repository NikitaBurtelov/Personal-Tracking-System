package org.pts.document.storage.domain.document.service.security;

public interface DocumentKeyEncryptionService {
    String encryptDocumentKey(byte[] data);

    byte[] decryptDocumentKey(String cipherData);
}