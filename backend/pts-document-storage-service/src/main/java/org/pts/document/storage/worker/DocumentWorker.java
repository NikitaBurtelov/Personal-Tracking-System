package org.pts.document.storage.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.OutboxJobStatus;
import org.pts.document.storage.model.enums.OutboxJobType;
import org.pts.document.storage.service.DocumentManagerService;
import org.pts.document.storage.service.dto.UploadResult;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentWorker {
    private final DocumentManagerService documentManagerService;
    private final JobManagerService jobManagerService;

    private final ThreadPoolTaskExecutor uploadDocumentProcessExecutor;
    private final ThreadPoolTaskExecutor getDocumentProcessExecutor;
    private final Executor taskProcessExecutor; // virtual thread

    private final Semaphore uploadDocumentProcessSemaphore;
    private final Semaphore deleteDocumentProcessSemaphore;
    private final Semaphore getDocumentProcessSemaphore;
    private final Semaphore updateJobStatusProcessSemaphore;

    @Scheduled(fixedDelay = 1000)
    public void uploadDocumentProcess() {
        if (!uploadDocumentProcessSemaphore.tryAcquire()) {
            return;
        }

        uploadDocumentProcessExecutor.execute(() -> {
            try {
                uploadProcessing();
            } finally {
                uploadDocumentProcessSemaphore.release();
            }
        });
    }

    @Scheduled(fixedDelay = 2000)
    public void getDocumentProcess() {

        if (!getDocumentProcessSemaphore.tryAcquire()) {
            return;
        }

        getDocumentProcessExecutor.execute(() -> {
            try {
                getDocumentProcessing();
            } finally {
                getDocumentProcessSemaphore.release();
            }
        });
    }

    @Scheduled(fixedDelay = 10000)
    public void jobProcess() {

        if (!updateJobStatusProcessSemaphore.tryAcquire()) {
            return;
        }

        taskProcessExecutor.execute(() -> {
            try {
            } finally {
                updateJobStatusProcessSemaphore.release();
            }
        });
    }

    private void uploadProcessing() {
        var jobs = jobManagerService.takeForProcessing(
                OutboxJobType.UPLOAD,
                OutboxJobStatus.NEW,
                10
        );

        var jobsId = jobs.keySet().stream().map(OutboxJobEntity::getId).toList();

        log.info("Tasks: {} have been accepted for processing.", jobsId);

        try {
            jobs.forEach((job, items) -> {
                var docs = items.stream().map(OutboxJobItemEntity::getDocumentId).toList();

                var itemsDocsMap = items.stream()
                        .collect(
                                Collectors.toMap(
                                        OutboxJobItemEntity::getDocumentId,
                                        item -> item)
                        );

                var results = documentManagerService.uploadDocumentAsync(docs);

                identifyAndUpdateStatus(job, itemsDocsMap, results);
            });
        } catch (Exception e) {
            log.error("Failed to complete tasks: {} ", jobsId, e);
            markFailed(
                    jobs.keySet().stream().map(OutboxJobEntity::getId).toList(),
                    jobs.values().stream().flatMap(List::stream).map(OutboxJobItemEntity::getId).toList());
        }
    }

    private void getDocumentProcessing() {
        var jobs = jobManagerService.takeForProcessing(
                OutboxJobType.GET,
                OutboxJobStatus.NEW,
                10
        );

        jobs.forEach((job, items) -> {
            try {
                var docs = items.stream().map(OutboxJobItemEntity::getDocumentId).toList();

                var itemsDocsMap = items.stream()
                        .collect(
                                Collectors.toMap(
                                        OutboxJobItemEntity::getDocumentId,
                                        item -> item)
                        );

                var results = documentManagerService.getDocumentAsync(docs);

                identifyAndUpdateStatus(job, itemsDocsMap, results);

            } catch (Exception e) {
                markFailed(job, items);
            }
        });
    }

    private void identifyAndUpdateStatus(
            OutboxJobEntity job,
            Map<UUID, OutboxJobItemEntity> itemsDocsMap,
            List<UploadResult> results
    ) {
        Map<Long, OutboxJobStatus> itemsStatusMap = new java.util.HashMap<>(Collections.emptyMap());
        AtomicReference<OutboxJobStatus> jobStatus = new AtomicReference<>(OutboxJobStatus.DONE);

        results.forEach(result -> {
            var item = itemsDocsMap.get(result.docId());

            if (result.result() == null) {
                jobStatus.set(OutboxJobStatus.FAILED);
                itemsStatusMap.put(
                        item.getId(),
                        OutboxJobStatus.FAILED
                );
            } else {
                itemsStatusMap.put(
                        item.getId(),
                        OutboxJobStatus.DONE
                );
            }
        });

        jobManagerService.updateJobAndItemStatus(
                job,
                jobStatus.get(),
                itemsStatusMap
        );
    }

    private void markFailed(
            OutboxJobEntity job,
            List<OutboxJobItemEntity> items
    ) {
        jobManagerService.updateJobAndItemStatus(
                job.getId(),
                items.stream()
                        .map(OutboxJobItemEntity::getId)
                        .collect(Collectors.toList()),
                OutboxJobStatus.FAILED
        );
    }

    private void markFailed(
            List<Long> jobsId,
            List<Long> itemsId
    ) {
        jobManagerService.updateJobAndItemStatus(
                jobsId,
                itemsId,
                OutboxJobStatus.FAILED
        );
    }
}
