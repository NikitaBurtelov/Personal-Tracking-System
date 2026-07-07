package org.pts.document.storage.worker.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.UploadProcessResult;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.Status;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateStatusProcessing {
    private final JobManagerService jobManagerService;

    public void execute(UploadProcessResult uploadResult) {
        if (uploadResult == null || uploadResult.jobsData().isEmpty()) {
            return;
        }

        try {
            Map<Long, Status> jobStatusMap = new HashMap<>();
            Map<Long, Map<Long, Status>> itemsStatusByJob = new HashMap<>();

            for (var jobData : uploadResult.jobsData()) {
                var itemsStatusMap = new HashMap<Long, Status>();
                var itemsByDocId = new HashMap<UUID, OutboxJobItemEntity>();
                AtomicReference<Status> jobStatus = new AtomicReference<>(Status.DONE);

                jobData.getItems().forEach(item ->
                        itemsByDocId.put(item.getDocumentId(), item)
                );

                jobData.getResults().forEach(result -> {
                    var item = itemsByDocId.get(result.docId());
                    if (item != null) {
                        if (result.result() == null) {
                            jobStatus.set(Status.FAILED);
                            itemsStatusMap.put(item.getId(), Status.FAILED);
                        } else {
                            itemsStatusMap.put(item.getId(), Status.DONE);
                        }
                    }
                });

                jobStatusMap.put(jobData.getJob().getId(), jobStatus.get());
                itemsStatusByJob.put(jobData.getJob().getId(), itemsStatusMap);
            }

            jobManagerService.updateJobAndItemStatusBatch(jobStatusMap, itemsStatusByJob);
        } catch (Exception e) {
            log.error("Failed to update statuses", e);
        }
    }
}
