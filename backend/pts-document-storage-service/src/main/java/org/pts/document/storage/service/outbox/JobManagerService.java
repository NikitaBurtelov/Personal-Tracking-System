package org.pts.document.storage.service.outbox;

import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.OutboxJobStatus;
import org.pts.document.storage.model.enums.OutboxJobType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface JobManagerService {
    @Transactional
    void createGetDocumentJob(GetDocumentSourceRequest msg);

    @Transactional
    void createUploadDocumentJob(UploadDocumentCommand msg);

    @Transactional
    Map<OutboxJobEntity, List<OutboxJobItemEntity>> takeForProcessing(
            OutboxJobType type,
            OutboxJobStatus status,
            int limit
    );

    @Transactional
    void updateJobAndItemStatus(
            Long jobId,
            List<Long> itemsId,
            OutboxJobStatus status
    );

    @Transactional
    void updateJobAndItemStatus(
            Long jobId,
            OutboxJobStatus status,
            Map<Long, OutboxJobStatus> itemsStatusMap
    );
}
