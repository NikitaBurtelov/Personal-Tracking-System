package org.pts.document.storage.service.processing;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ProcessingOperationService {

    @Transactional
    void onBatchCompleted(UUID operationId);
}
