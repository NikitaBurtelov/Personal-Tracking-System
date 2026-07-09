package org.pts.document.storage.service.outbox;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ProcessingOperationManager {

    @Transactional
    void onJobCompleted(UUID operationId);
}
