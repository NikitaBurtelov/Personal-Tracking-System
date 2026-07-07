package org.pts.document.storage.service.document;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.repository.DocumentRepository;
import org.pts.document.storage.repository.OutboxItemRepository;
import org.pts.document.storage.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentJobManagerImpl implements DocumentJobManager {
    private final OutboxRepository outboxRepository;
    private final OutboxItemRepository outboxItemRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    @Override
    public List<DocumentEntity> getDocumentsByRequestId(UUID requestId) {
        var jobs = outboxRepository.findAllByRequestId(requestId);

        var items = outboxItemRepository.findAllByJobIdIn(
                jobs.stream()
                        .map(OutboxJobEntity::getId)
                        .toList()
        );

        return documentRepository.findAllByIdIn(
                items.stream()
                        .map(OutboxJobItemEntity::getDocumentId)
                        .toList()
        );
    }
}

