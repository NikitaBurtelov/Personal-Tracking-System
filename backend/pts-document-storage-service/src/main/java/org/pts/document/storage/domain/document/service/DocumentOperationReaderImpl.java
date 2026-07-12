package org.pts.document.storage.domain.document.service;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.domain.document.entity.DocumentEntity;
import org.pts.document.storage.domain.processing.entity.ProcessingBatchEntity;
import org.pts.document.storage.domain.processing.entity.ProcessingTaskEntity;
import org.pts.document.storage.domain.document.repository.DocumentRepository;
import org.pts.document.storage.domain.processing.repository.ProcessingBatchRepository;
import org.pts.document.storage.domain.processing.repository.ProcessingTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentOperationReaderImpl implements DocumentOperationReader {
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

