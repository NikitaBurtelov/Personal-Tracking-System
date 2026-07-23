package org.pts.document.storage.vault;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.vault.exception.VaultTransitException;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VaultTransitClientImpl implements VaultTransitClient {
    private final VaultTemplate vaultTemplate;

    private static final String ENCRYPT_PATH =
            "transit/encrypt/";
    private static final String DECRYPT_PATH =
            "transit/decrypt/";


    @Override
    public String encrypt(
            String keyName,
            byte[] data
    ) {
        var plaintext =
                Base64.getEncoder()
                        .encodeToString(data);

        var response =
                vaultTemplate.write(
                        ENCRYPT_PATH + keyName,
                        Map.of(
                                "plaintext",
                                plaintext
                        )
                );

        if (response == null) {
            throw new VaultTransitException(
                    "Vault returned empty response"
            );
        }

        return response
                .getRequiredData()
                .get("ciphertext")
                .toString();
    }

    @Override
    public byte[] decrypt(
            String keyName,
            String cipherData
    ) {
        var response =
                vaultTemplate.write(
                        DECRYPT_PATH + keyName,
                        Map.of(
                                "ciphertext",
                                cipherData
                        )
                );

        if (response == null) {
            throw new VaultTransitException(
                    "Vault returned empty response"
            );
        }

        var plaintext =
                response
                        .getRequiredData()
                        .get("plaintext")
                        .toString();

        return Base64.getDecoder()
                .decode(plaintext);
    }
}