package org.pts.document.storage.service.document;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.entity.ProcessingBatchEntity;
import org.pts.document.storage.model.entity.ProcessingTaskEntity;
import org.pts.document.storage.repository.DocumentRepository;
import org.pts.document.storage.repository.ProcessingBatchRepository;
import org.pts.document.storage.repository.ProcessingTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentOperationManagerImpl implements DocumentOperationManager {
    private final ProcessingBatchRepository processingBatchRepository;
    private final ProcessingTaskRepository processingTaskRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    @Override
    public List<DocumentEntity> getDocumentsByOperationId(UUID operationId) {
        var batches = processingBatchRepository.findAllByOperationId(operationId);

        var tasks = processingTaskRepository.findAllByBatchIdIn(
                batches.stream()
                        .map(ProcessingBatchEntity::getId)
                        .toList()
        );

        return documentRepository.findAllByIdIn(
                tasks.stream()
                        .map(ProcessingTaskEntity::getDocumentId)
                        .toList()
        );
    }
}

