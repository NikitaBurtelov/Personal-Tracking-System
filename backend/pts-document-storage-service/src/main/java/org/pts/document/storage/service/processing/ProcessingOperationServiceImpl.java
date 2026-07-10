package org.pts.document.storage.service.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.pts.document.storage.repository.OutboxEventRepository;
import org.pts.document.storage.repository.ProcessingOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingOperationServiceImpl implements ProcessingOperationService {
    private final ProcessingOperationRepository processingOperationRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    @Override
    public void onBatchCompleted(UUID operationId) {
        String status = processingOperationRepository.completeBatch(operationId);

        if (status.equals(ProcessingStatus.DONE.getStatus())) {
            log.debug("Job completed successfully, operationId: {}", operationId);
            outboxEventRepository.save(
                    OutboxEventEntity.builder()
                            .operationId(operationId)
                            .published(false)
                            .build()
            );
        }
    }
}
