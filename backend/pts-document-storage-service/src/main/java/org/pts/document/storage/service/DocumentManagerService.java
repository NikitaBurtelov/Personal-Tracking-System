package org.pts.document.storage.service;

import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.service.dto.UploadResult;

import java.util.List;
import java.util.UUID;

public interface DocumentManagerService {
    List<UploadResult> uploadDocumentAsync(List<UUID> documentsId);

    List<UploadResult> getDocumentAsync(List<UUID> documentsId);
}
