package org.pts.document.storage.worker.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.JobUploadData;
import org.pts.document.storage.model.dto.UploadProcessResult;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.JobStatus;
import org.pts.document.storage.model.enums.JobType;
import org.pts.document.storage.service.document.DocumentManagerService;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UploadDocumentProcessing {
    private final DocumentManagerService documentManagerService;
    private final JobManagerService jobManagerService;

    public UploadProcessResult execute() {
        var jobs = jobManagerService.takeForProcessing(
                JobType.UPLOAD,
                JobStatus.NEW,
                10
        );

        var jobIds = jobs
                .keySet()
                .stream()
                .map(OutboxJobEntity::getId)
                .toList();

        if (jobs.isEmpty()) {
            return null;
        }

        log.info(
                "Tasks: {} have been accepted for processing.",
                jobIds
        );

        try {
            List<JobUploadData> jobsData = new ArrayList<>();

            jobs.forEach((job, items) -> {
                var docs = items.stream()
                        .map(OutboxJobItemEntity::getDocumentId
                        )
                        .toList();
                var results = documentManagerService.uploadDocumentsAsync(docs);

                jobsData.add(
                        JobUploadData.builder()
                                .job(job)
                                .items(items)
                                .results(results)
                                .build());
            });

            return new UploadProcessResult(jobsData);
        } catch (Exception e) {
            log.error("Failed to upload documents for tasks: {} ", jobIds, e);
            markFailed(jobIds, jobs.values().stream()
                    .flatMap(List::stream)
                    .map(OutboxJobItemEntity::getId)
                    .toList());
            return null;
        }
    }

    private void markFailed(List<Long> jobIds, List<Long> itemIds) {
        jobManagerService.updateJobAndItemStatus(jobIds, itemIds, JobStatus.FAILED);
    }
}

