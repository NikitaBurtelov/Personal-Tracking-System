package org.pts.document.storage.vault;

public interface VaultTransitClient {
    String encrypt(
            String keyName,
            byte[] data
    );

    byte[] decrypt(
            String keyName,
            String cipherData
    );
}
