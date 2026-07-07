package org.pts.document.storage.worker.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.JobUploadData;
import org.pts.document.storage.model.dto.UploadProcessResult;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.Status;
import org.pts.document.storage.model.enums.Type;
import org.pts.document.storage.service.DocumentManagerService;
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
                Type.UPLOAD,
                Status.NEW,
                10
        );

        var jobsId = jobs
                .keySet()
                .stream()
                .map(OutboxJobEntity::getId)
                .toList();

        if (jobs.isEmpty()) {
            return null;
        }

        log.info(
                "Tasks: {} have been accepted for processing.",
                jobsId
        );

        try {
            List<JobUploadData> jobsData = new ArrayList<>();

            jobs.forEach((job, items) -> {
                var docs = items.stream()
                        .map(OutboxJobItemEntity::getDocumentId
                        )
                        .toList();
                var results = documentManagerService.uploadDocumentAsync(docs);

                jobsData.add(
                        JobUploadData.builder()
                                .job(job)
                                .items(items)
                                .results(results)
                                .build());
            });

            return new UploadProcessResult(jobsData);
        } catch (Exception e) {
            log.error("Failed to upload documents for tasks: {} ", jobsId, e);
            markFailed(jobsId, jobs.values().stream()
                    .flatMap(List::stream)
                    .map(item -> item.getId())
                    .toList());
            return null;
        }
    }

    private void markFailed(List<Long> jobsId, List<Long> itemsId) {
        jobManagerService.updateJobAndItemStatus(jobsId, itemsId, Status.FAILED);
    }
}

