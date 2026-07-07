package org.pts.document.storage.service.document;

import org.pts.document.storage.service.dto.UploadResult;

import java.util.List;
import java.util.UUID;

public interface DocumentManagerService {
    List<UploadResult> uploadDocumentsAsync(List<UUID> documentIds);

    List<UploadResult> fetchDocumentsAsync(List<UUID> documentIds);
}

