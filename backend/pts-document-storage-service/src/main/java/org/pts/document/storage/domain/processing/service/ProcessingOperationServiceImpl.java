package org.pts.document.storage.domain.processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.domain.enums.ProcessingStatus;
import org.pts.document.storage.domain.outbox.repository.OutboxEventRepository;
import org.pts.document.storage.domain.processing.repository.ProcessingOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingOperationServiceImpl implements ProcessingOperationService {
    private final ProcessingOperationRepository processingOperationRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public ProcessingStatus onBatchCompleted(UUID operationId) {
        String status = processingOperationRepository.completeBatch(operationId);

        if (status.equals(ProcessingStatus.DOCUMENTS_UPLOADED.getStatus())) {
            log.debug("Job completed successfully, operationId: {}", operationId);

            return ProcessingStatus.DOCUMENTS_UPLOADED;
        } else
            return ProcessingStatus.PROCESSING;
    }
}
