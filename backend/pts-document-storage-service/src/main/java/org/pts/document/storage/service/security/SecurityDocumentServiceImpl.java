package org.pts.document.storage.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.EncryptedPayload;
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
    private final SecretKey masterKey;

    @Override
    public CipherInputStream decryptByStream(
            InputStream encryptedStream,
            byte[] encryptedDataKey,
            byte[] iv
    ) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        var dataKey = decryptDataKey(encryptedDataKey, iv);

        cipher.init(
                Cipher.DECRYPT_MODE,
                dataKey,
                new GCMParameterSpec(128, iv)
        );

        return new CipherInputStream(encryptedStream, cipher);
    }

    @Override
    public Pair<CipherInputStream, EncryptedPayload> encryptByStream(
            InputStream objectStream
    ) throws Exception {
        var keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        var secretKey = keyGenerator.generateKey();
        var encryptDataKey = encryptDataKey(secretKey, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKey,
                new GCMParameterSpec(128, iv)
        );

        CipherInputStream cipherStream = new CipherInputStream(objectStream, cipher);


        return Pair.of(
                cipherStream,
                EncryptedPayload.builder()
                        .encryptedDataKey(encryptDataKey)
                        .iv(iv)
                        .build()
        );
    }

    private byte[] encryptDataKey(SecretKey dataKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                Cipher.ENCRYPT_MODE,
                masterKey,
                new GCMParameterSpec(128, iv)
        );

        return cipher.doFinal(dataKey.getEncoded());
    }

    private SecretKey decryptDataKey(byte[] dataKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(
                Cipher.DECRYPT_MODE,
                masterKey,
                new GCMParameterSpec(128, iv)
        );

        return new SecretKeySpec(cipher.doFinal(dataKey), "AES");
    }
}