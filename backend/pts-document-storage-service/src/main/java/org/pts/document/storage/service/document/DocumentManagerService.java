package org.pts.document.storage.service.document;

import org.pts.document.storage.service.dto.DocumentContext;

import java.util.List;
import java.util.UUID;

public interface DocumentManagerService {
    List<DocumentContext> uploadDocumentsAsync(List<UUID> documentIds);

    List<DocumentContext> fetchDocumentsAsync(List<UUID> documentIds);
}

