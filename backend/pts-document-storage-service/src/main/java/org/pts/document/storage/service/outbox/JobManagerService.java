package org.pts.document.storage.service.outbox;

import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.Status;
import org.pts.document.storage.model.enums.Type;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface JobManagerService {
    @Transactional
    void createGetDocumentJob(GetDocumentSourceRequest msg);

    @Transactional
    void createUploadDocumentJob(UploadDocumentCommand msg);

    @Transactional
    Map<OutboxJobEntity, List<OutboxJobItemEntity>> takeForProcessing(
            Type type,
            Status status,
            int limit
    );

    @Transactional
    void updateJobAndItemStatus(
            Long jobId,
            List<Long> itemsId,
            Status status
    );

    @Transactional
    void updateJobAndItemStatus(
            Long jobId,
            Status status,
            Map<Long, Status> itemsStatusMap
    );

    @Transactional
    void updateJobAndItemStatus(
            OutboxJobEntity job,
            Status status,
            Map<Long, Status> itemsStatusMap
    );

    @Transactional
    void updateJobAndItemStatus(
            List<Long> jobId,
            List<Long> itemsId,
            Status status
    );

    @Transactional
    void updateJobAndItemStatusBatch(
            Map<Long, Status> jobStatusMap,
            Map<Long, Map<Long, Status>> itemsStatusByJob
    );

    List<DocumentEntity> getDocument(UUID requestId);
}
