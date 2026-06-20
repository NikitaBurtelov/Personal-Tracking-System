package org.pts.document.storage.service.document;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.enums.DocumentStatus;
import org.pts.document.storage.repository.DocumentRepository;
import org.pts.document.storage.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentRepositoryServiceImpl implements DocumentRepositoryService {
    private final DocumentRepository documentRepository;
    private final OutboxRepository outboxRepository;

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
    public void updateStatus(UUID idDocument, DocumentStatus documentStatus) {
        var document = documentRepository.findById(idDocument)
                .orElseThrow(() -> new EntityNotFoundException("Document not found id: " + idDocument));

        document.setStatus(documentStatus);
    }
}
