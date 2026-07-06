package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.repository.ProcessingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestManagerServiceImpl implements RequestManagerService {
    private final ProcessingRequestRepository requestRepository;

    @Transactional
    @Override
    public void onJobCompleted(UUID requestId) {
        String status = requestRepository.completeJob(requestId);

        if ("COMPLETED".equals(status)) {

            //TODO sending result

        }
    }
}
