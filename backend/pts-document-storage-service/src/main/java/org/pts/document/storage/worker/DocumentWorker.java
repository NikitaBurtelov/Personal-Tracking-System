package org.pts.document.storage.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.Status;
import org.pts.document.storage.model.enums.Type;
import org.pts.document.storage.service.DocumentManagerService;
import org.pts.document.storage.service.dto.UploadResult;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.pts.document.storage.worker.process.PublishingEventProcessing;
import org.pts.document.storage.worker.process.UpdateStatusProcessing;
import org.pts.document.storage.worker.process.UploadDocumentProcessing;
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

    private final PublishingEventProcessing publishingEventProcessing;
    private final UploadDocumentProcessing uploadDocumentProcessing;
    private final UpdateStatusProcessing updateStatusProcessing;

    private final Semaphore publicationEventProcessSemaphore;
    private final Semaphore uploadDocumentProcessSemaphore;
    private final Semaphore deleteDocumentProcessSemaphore;
    private final Semaphore getDocumentProcessSemaphore;
    private final Semaphore updateJobStatusProcessSemaphore;

    @Scheduled(fixedDelay = 1000)
    public void publishingEventProcess() {
        if (!publicationEventProcessSemaphore.tryAcquire()) {
            return;
        }

        taskProcessExecutor.execute(() -> {
            try {
                publishingEventProcessing.execute();
            } finally {
                publicationEventProcessSemaphore.release();
            }
        });
    }

    @Scheduled(fixedDelay = 1000)
    public void uploadDocumentProcess() {
        if (!uploadDocumentProcessSemaphore.tryAcquire()) {
            return;
        }

        uploadDocumentProcessExecutor.execute(() -> {
            try {
                // Stage 1: upload documents
                var uploadResult = uploadDocumentProcessing.execute();

                // Stage 2: update statuses based on upload results
                if (uploadResult != null) {
                    updateStatusProcessing.execute(uploadResult);
                }
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
                //TODO
            } finally {
                updateJobStatusProcessSemaphore.release();
            }
        });
    }

    private void getDocumentProcessing() {
        var jobs = jobManagerService.takeForProcessing(
                Type.GET,
                Status.NEW,
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
        Map<Long, Status> itemsStatusMap = new java.util.HashMap<>(Collections.emptyMap());
        AtomicReference<Status> jobStatus = new AtomicReference<>(Status.DONE);

        results.forEach(result -> {
            var item = itemsDocsMap.get(result.docId());

            if (result.result() == null) {
                jobStatus.set(Status.FAILED);
                itemsStatusMap.put(
                        item.getId(),
                        Status.FAILED
                );
            } else {
                itemsStatusMap.put(
                        item.getId(),
                        Status.DONE
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
                Status.FAILED
        );
    }

    private void markFailed(
            List<Long> jobsId,
            List<Long> itemsId
    ) {
        jobManagerService.updateJobAndItemStatus(
                jobsId,
                itemsId,
                Status.FAILED
        );
    }
}
