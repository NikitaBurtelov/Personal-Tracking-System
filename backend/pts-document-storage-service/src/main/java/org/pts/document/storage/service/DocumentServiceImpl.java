package org.pts.document.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.pts.document.storage.config.properties.MinIOProperties;
import org.pts.document.storage.dto.DocumentUploadRequest;
import org.pts.document.storage.dto.DocumentUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final StorageService storageService;
    private final MinIOProperties minIOProperties;

    @Override
    public DocumentUploadResponse upload(MultipartFile filePart, DocumentUploadRequest request) {
        try {
            var id = UUID.randomUUID();
            var key = id + "_" + filePart.getOriginalFilename();
            var contentType = filePart.getContentType();

            storageService.putObject(
                    PutObjectRequest.builder()
                            .bucket(minIOProperties.getImg().getBucketName())
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromFile(filePart.getResource().getFile())
            );

            return new DocumentUploadResponse(key);
        } catch (IOException e) {
            log.info(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}