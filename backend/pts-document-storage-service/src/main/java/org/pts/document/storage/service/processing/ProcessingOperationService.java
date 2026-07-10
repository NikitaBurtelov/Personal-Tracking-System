package org.pts.document.storage.service.processing;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface ProcessingOperationService {

    @Transactional
    Optional<UUID> onBatchCompleted(UUID operationId);
}
