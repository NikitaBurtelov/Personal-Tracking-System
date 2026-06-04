package org.pts.document.storage.service;

import org.pts.document.storage.dto.DocumentUploadRequest;
import org.pts.document.storage.dto.DocumentUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentService {
    DocumentUploadResponse upload(MultipartFile filePart, DocumentUploadRequest request);
}
