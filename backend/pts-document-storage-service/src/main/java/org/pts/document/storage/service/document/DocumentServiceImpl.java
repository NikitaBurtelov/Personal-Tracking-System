package org.pts.document.storage.service.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.config.minio.MinIOProperties;
import org.pts.document.storage.model.DocumentEntity;
import org.pts.document.storage.service.security.SecurityDocumentService;
import org.pts.document.storage.service.storage.StorageService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final StorageService storageService;
    private final SecurityDocumentService securityDocumentService;
    private final DocumentRepositoryService documentRepositoryService;
    private final MinIOProperties minIOProperties;

    @Override
    public String getDocument(String key, String bucket) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        var getHeadObjectRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        var headObject = storageService.getHeadObject(getHeadObjectRequest);
        var metadata = headObject.metadata();

        var originalFileName = metadata.get("original-file-name");
        var originalContentType = metadata.get("original-content-type");
        var encryptedDataKey = decode(metadata.get("encrypted-data-key"));
        var iv = decode(metadata.get("iv"));

        var s3ObjectStream = storageService.getObjectStream(getObjectRequest);

        var decryptS3ObjectStream = securityDocumentService.decryptByStream(
                s3ObjectStream,
                encryptedDataKey,
                iv
        );

        try (s3ObjectStream; decryptS3ObjectStream) {
            var decryptS3ObjectKey = UUID.randomUUID() + originalFileName;

            storageService.putObject(
                    PutObjectRequest.builder()
                            .bucket(minIOProperties.getDocumentTempBucket().getBucketName())
                            .key(decryptS3ObjectKey)
                            .contentType(originalContentType)
                            .build(),
                    RequestBody.fromInputStream(decryptS3ObjectStream, -1)
            );

            return decryptS3ObjectKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String upload(UUID id) throws Exception {
        var document = documentRepositoryService.get(id);

        var key = document.getTempKey();
        var bucket = document.getTempBucket();

        var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        var getHeadObjectRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        var headObject = storageService.getHeadObject(getHeadObjectRequest);
        var metadata = headObject.metadata();

        var originalFileName = metadata.get("original-file-name");
        var contentType = headObject.contentType();

        var s3ObjectStream = storageService.getObjectStream(getObjectRequest);

        var encryptStreamPair = securityDocumentService.encryptByStream(
                s3ObjectStream
        );
        var encrypts3ObjectStream = encryptStreamPair.getFirst();
        var encryptedPayload = encryptStreamPair.getSecond();

        var documentEntity = DocumentEntity.builder()
                .id(id)
                .key(key)
                .encryptedFileKey(encryptedPayload.encryptedDataKey())
                .iv(encryptedPayload.iv())
                .status("PROCESSING")
                .build();

        documentRepositoryService.save(
                documentEntity
        );

        try (s3ObjectStream; encrypts3ObjectStream) {
            storageService.putObject(
                    PutObjectRequest.builder()
                            .bucket(minIOProperties.getDocumentPersistenceBucket().getBucketName())
                            .key(key)
                            .metadata(Map.of(
                                    "original-content-type", contentType,
                                    "original-key", key,
                                    "original-file-name", originalFileName,
                                    "iv", encode(encryptedPayload.iv()),
                                    "encrypted-data-key", encode(encryptedPayload.encryptedDataKey())
                            ))
                            .contentType("application/octet-stream")
                            .build(),
                    RequestBody.fromInputStream(encrypts3ObjectStream, -1)
            );

            documentRepositoryService.updateStatus(id, "UPLOADED");

            return key;
        } catch (Exception e) {
            documentRepositoryService.updateStatus(id, "FAILED");
            throw new RuntimeException(e);
        }
    }

    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }
}