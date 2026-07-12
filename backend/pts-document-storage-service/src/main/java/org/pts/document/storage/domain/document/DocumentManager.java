package org.pts.document.storage.domain.document;

import org.pts.document.storage.domain.context.DocumentContext;

import java.util.List;
import java.util.UUID;

public interface DocumentManager {
    List<DocumentContext> uploadDocumentsAsync(List<UUID> documentIds);

    List<DocumentContext> fetchDocumentsAsync(List<UUID> documentIds);
}

