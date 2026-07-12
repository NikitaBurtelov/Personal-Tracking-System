package org.pts.document.storage.domain.document.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.domain.document.entity.DocumentEntity;
import org.pts.document.storage.domain.document.repository.DocumentRepository;
import org.pts.document.storage.domain.enums.DocumentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentPersistenceServiceImpl implements DocumentPersistenceService {
    private final DocumentRepository documentRepository;

    @Transactional
    @Override
    public void save(DocumentEntity documentEntity) {
        documentRepository.save(documentEntity);
    }

    @Override
    public DocumentEntity get(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found id: " + id));
    }

    @Transactional
    @Override
    public void updateStatus(UUID documentId, DocumentStatus documentStatus) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found id: " + documentId));

        document.setStatus(documentStatus);
    }
}
