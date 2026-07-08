package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.JobExecutionResult;
import org.pts.document.storage.model.enums.JobStatus;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateJobStatusExecutor {
    private final JobManagerService jobManagerService;

    public void execute(List<JobExecutionResult> jobExecutionResults) {
        if (jobExecutionResults == null || jobExecutionResults.isEmpty()) {
            return;
        }

        try {
            Map<Long, JobStatus> jobStatusMap = new HashMap<>();
            Map<Long, JobStatus> itemsStatusMap = new HashMap<>();

            for (var jobData : jobExecutionResults) {
                AtomicReference<JobStatus> jobStatus = new AtomicReference<>(JobStatus.DONE);

                jobData.items().forEach(item -> {
                    if (item.result() == null) {
                        jobStatus.set(JobStatus.FAILED);
                        itemsStatusMap.put(item.itemId(), JobStatus.FAILED);
                    } else {
                        itemsStatusMap.put(item.itemId(), JobStatus.DONE);
                    }
                });

                jobStatusMap.put(jobData.jobId(), jobStatus.get());
            }

            jobManagerService.updateJobAndItemStatusBatch(
                    jobStatusMap,
                    itemsStatusMap
            );
        } catch (Exception e) {
            log.error("Failed to update statuses", e);
        }
    }
}
