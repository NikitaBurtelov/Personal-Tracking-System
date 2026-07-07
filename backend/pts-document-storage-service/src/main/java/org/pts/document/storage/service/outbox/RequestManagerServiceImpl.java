package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.pts.document.storage.model.enums.Type;
import org.pts.document.storage.repository.OutboxEventRepository;
import org.pts.document.storage.repository.ProcessingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestManagerServiceImpl implements RequestManagerService {
    private final ProcessingRequestRepository requestRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    @Override
    public void onJobCompleted(UUID requestId) {
        String status = requestRepository.completeJob(requestId);

        if (status.equals(Type.DONE.getType())) {
            outboxEventRepository.save(
                    OutboxEventEntity.builder()
                            .requestId(requestId)
                            .published(false)
                            .build()
            );
        }
    }
}
