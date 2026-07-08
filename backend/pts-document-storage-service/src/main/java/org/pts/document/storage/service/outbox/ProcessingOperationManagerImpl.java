package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.pts.document.storage.model.enums.JobStatus;
import org.pts.document.storage.repository.OutboxEventRepository;
import org.pts.document.storage.repository.ProcessingOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingOperationManagerImpl implements ProcessingOperationManager {
    private final ProcessingOperationRepository requestRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    @Override
    public void onJobCompleted(UUID operationId) {
        String status = requestRepository.completeJob(operationId);

        if (status.equals(JobStatus.DONE.getStatus())) {
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
