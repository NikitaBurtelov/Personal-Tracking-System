package org.pts.document.storage.service.processing;

import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.pts.document.storage.model.enums.ProcessingType;
import org.pts.document.storage.service.dto.BatchContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ProcessingOperationManager {
    @Transactional
    void createGetDocumentTask(GetDocumentSourceRequest msg);

    @Transactional
    void createUploadDocumentTask(UploadDocumentCommand msg);

    @Transactional
    Map<ProcessingStatus, List<BatchContext>> takeForProcessing(
            ProcessingType type,
            int limit
    );

    @Transactional
    void updateBatchAndTaskStatus(
            List<Long> batchIds,
            List<Long> taskIds,
            ProcessingStatus status
    );

    @Transactional
    void updateBatchAndTaskStatus(
            List<BatchContext> batchContexts
    );

    @Transactional
    void updateBatchStatus(
            List<BatchContext> batchContexts
    );

    @Transactional
    void onBatchCompleted(List<BatchContext> batchContexts);
}
