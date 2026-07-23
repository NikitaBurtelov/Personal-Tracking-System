package org.pts.document.storage.domain.document.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.domain.context.EncryptedPayload;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityDocumentServiceImpl implements SecurityDocumentService {
    private final DocumentKeyEncryptionService documentKeyEncryptionService;

    @Override
    public CipherInputStream decryptByStream(
            InputStream encryptedStream,
            String encryptedDataKey,
            byte[] iv
    ) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        var keyBytes = documentKeyEncryptionService.decryptDocumentKey(encryptedDataKey);
        var dataKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.DECRYPT_MODE,
                dataKey,
                new GCMParameterSpec(128, iv)
        );

        return new CipherInputStream(
                encryptedStream,
                cipher
        );
    }

    @Override
    public Pair<CipherInputStream, EncryptedPayload> encryptByStream(
            InputStream objectStream
    ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        var keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);

        var secretKey = keyGenerator.generateKey();

        byte[] iv = new byte[12];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKey,
                new GCMParameterSpec(128, iv)
        );

        CipherInputStream cipherStream = new CipherInputStream(objectStream, cipher);

        var encryptDataKey = documentKeyEncryptionService.encryptDocumentKey(
                secretKey.getEncoded()
        );

        return Pair.of(
                cipherStream,
                EncryptedPayload.builder()
                        .encryptedDataKey(encryptDataKey)
                        .iv(iv)
                        .build()
        );
    }
}