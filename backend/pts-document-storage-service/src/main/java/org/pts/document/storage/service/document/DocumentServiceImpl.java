package org.pts.document.storage.service.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.config.minio.MinIOProperties;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.enums.DocumentStatus;
import org.pts.document.storage.service.document.security.SecurityDocumentService;
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
import java.util.Optional;
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
    public String getDocument(UUID docId) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DocumentEntity document = documentRepositoryService.get(docId);
        var key = document.getObjectKey();
        var bucket = minIOProperties.getDocumentPersistenceBucket();

        var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket.getBucketName())
                .key(key)
                .build();
        var getHeadObjectRequest = HeadObjectRequest.builder()
                .bucket(bucket.getBucketName())
                .key(key)
                .build();

        var headObject = storageService.getHeadObject(getHeadObjectRequest);
        var metadata = headObject.metadata();

        var originalFileName = metadata.get("original-file-name");
        var originalContentType = metadata.get("original-content-type");
        var encryptedDataKey = document.getEncryptedDataKey();
        var iv = document.getInitializationVector();

        var s3ObjectStream = storageService.getObjectStream(getObjectRequest);

        var decryptS3ObjectStream = securityDocumentService.decryptByStream(
                s3ObjectStream,
                encryptedDataKey,
                iv
        );
        var tempKey = UUID.randomUUID() + originalFileName;

        try (s3ObjectStream; decryptS3ObjectStream) {
            storageService.putObject(
                    PutObjectRequest.builder()
                            .bucket(minIOProperties.getDocumentTempBucket().getBucketName())
                            .key(tempKey)
                            .contentType(originalContentType)
                            .build(),
                    RequestBody.fromContentProvider(
                            () -> decryptS3ObjectStream,
                            "application/octet-stream"
                    )
            );


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        document.setTransferObjectKey(tempKey);
        document.setTransferBucket(minIOProperties.getDocumentTempBucket().getBucketName());
        documentRepositoryService.save(document);

        return tempKey;
    }

    @Override
    public String upload(UUID id) throws Exception {
        var document = documentRepositoryService.get(id);

        var tempKey = document.getTransferObjectKey();
        var tempBucket = document.getTransferBucket();

        var getObjectRequest = GetObjectRequest.builder()
                .bucket(tempBucket)
                .key(tempKey)
                .build();
        var getHeadObjectRequest = HeadObjectRequest.builder()
                .bucket(tempBucket)
                .key(tempKey)
                .build();

        var headObject = storageService.getHeadObject(getHeadObjectRequest);
        var metadata = headObject.metadata();
        var contentType = headObject.contentType();
        var originalFileName = Optional.ofNullable(metadata.get("original-file-name"))
                .orElse(contentType.replace("/", "."));

        var s3ObjectStream = storageService.getObjectStream(getObjectRequest);

        var encryptStreamPair = securityDocumentService.encryptByStream(
                s3ObjectStream
        );
        var encrypts3ObjectStream = encryptStreamPair.getFirst();
        var encryptedPayload = encryptStreamPair.getSecond();

        var persistenceBucket = minIOProperties.getDocumentPersistenceBucket().getBucketName();
        var persistenceKey = UUID.randomUUID() + originalFileName;

        document.setObjectKey(persistenceKey);
        document.setEncryptedDataKey(encryptedPayload.encryptedDataKey());
        document.setInitializationVector(encryptedPayload.iv());
        document.setStatus(DocumentStatus.UPLOADING);

        documentRepositoryService.save(
                document
        );

        try (s3ObjectStream; encrypts3ObjectStream) {
            storageService.putObject(
                    PutObjectRequest.builder()
                            .bucket(persistenceBucket)
                            .key(persistenceKey)
                            .metadata(Map.of(
                                    "original-content-type", contentType != null ? contentType : "",
                                    "original-tempKey", tempKey != null ? tempKey : "",
                                    "original-file-name", originalFileName != null ? originalFileName : "",
                                    "iv", encode(encryptedPayload.iv()),
                                    "encrypted-data-tempKey", encode(encryptedPayload.encryptedDataKey())
                            ))
                            .contentType("application/octet-stream")
                            .build(),
                    RequestBody.fromContentProvider(
                            () -> encrypts3ObjectStream,
                            "application/octet-stream"
                    )
            );

            documentRepositoryService.updateStatus(id, DocumentStatus.UPLOADED);

            return persistenceKey;
        } catch (Exception e) {
            documentRepositoryService.updateStatus(id, DocumentStatus.FAILED);
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