package org.pts.document.storage.service.document;

import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.enums.DocumentStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface DocumentRepositoryService {
    @Transactional
    void save(DocumentEntity documentEntity);

    DocumentEntity get(UUID id);

    @Transactional
    void updateStatus(UUID idDocument, DocumentStatus documentStatus);
}
