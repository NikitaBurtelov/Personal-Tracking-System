package org.pts.document.storage.domain.document.service;

import org.pts.document.storage.domain.document.entity.DocumentEntity;
import org.pts.document.storage.domain.enums.DocumentStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface DocumentPersistenceService {
    @Transactional
    void save(DocumentEntity documentEntity);

    DocumentEntity get(UUID id);

    @Transactional
    void updateStatus(UUID documentId, DocumentStatus documentStatus);
}
