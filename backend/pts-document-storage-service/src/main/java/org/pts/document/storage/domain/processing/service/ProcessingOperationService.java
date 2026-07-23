package org.pts.document.storage.domain.processing.service;

import org.pts.document.storage.domain.enums.ProcessingStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ProcessingOperationService {

    @Transactional
    ProcessingStatus onBatchCompleted(UUID operationId);
}
