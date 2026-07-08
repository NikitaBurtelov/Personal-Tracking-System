package org.pts.document.storage.service.outbox;

import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.model.enums.JobStatus;
import org.pts.document.storage.model.enums.JobType;
import org.pts.document.storage.service.dto.JobContext;
import org.pts.document.storage.service.dto.JobItemContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface JobManagerService {
    @Transactional
    void createGetDocumentJob(GetDocumentSourceRequest msg);

    @Transactional
    void createUploadDocumentJob(UploadDocumentCommand msg);

    @Transactional
    Map<JobContext, List<JobItemContext>> takeForProcessing(
            JobType type,
            JobStatus status,
            int limit
    );

    @Transactional
    void updateJobAndItemStatus(
            List<Long> jobIds,
            List<Long> itemIds,
            JobStatus status
    );

    @Transactional
    void updateJobAndItemStatusBatch(
            Map<Long, JobStatus> jobStatusMap,
            Map<Long, JobStatus> itemsStatusByJob
    );
}
