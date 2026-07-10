package org.pts.document.storage.service.security;

import org.pts.document.storage.model.dto.DocumentEncryptedPayload;
import org.springframework.data.util.Pair;

import javax.crypto.BadPaddingException;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface SecurityDocumentService {
    CipherInputStream decryptByStream(
            InputStream encryptedStream,
            byte[] encryptedDataKey,
            byte[] iv
    ) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException;

    Pair<CipherInputStream, DocumentEncryptedPayload> encryptByStream(
            InputStream objectStream
    ) throws Exception;
}
