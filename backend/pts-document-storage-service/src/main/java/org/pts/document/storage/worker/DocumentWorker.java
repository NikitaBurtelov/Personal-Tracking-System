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

    private final ThreadPoolTaskExecutor uploadDocumentExecutor;
    private final ThreadPoolTaskExecutor getDocumentExecutor;
    private final Executor taskExecutor; // virtual thread

    private final Semaphore uploadDocumentJobSemaphore;
    private final Semaphore deleteDocumentJobSemaphore;
    private final Semaphore getDocumentJobSemaphore;
    private final Semaphore updateJobStatusSemaphore;

    @Scheduled(fixedDelay = 1000)
    public void uploadDocumentProcess() {

        if (!uploadDocumentJobSemaphore.tryAcquire()) {
            return;
        }

        uploadDocumentExecutor.execute(() -> {
            try {
                uploadDocumentExecutor.execute(this::uploadProcessing);
            } finally {
                uploadDocumentJobSemaphore.release();
            }
        });
    }

    @Scheduled(fixedDelay = 2000)
    public void getDocumentProcess() {

        if (!getDocumentJobSemaphore.tryAcquire()) {
            return;
        }

        getDocumentExecutor.execute(() -> {
            try {
                return;
            } finally {
                getDocumentJobSemaphore.release();
            }
        });
    }

    @Scheduled(fixedDelay = 10000)
    public void joProcess() {

        if (!updateJobStatusSemaphore.tryAcquire()) {
            return;
        }

        taskExecutor.execute(() -> {
            try {
                return;
            } finally {
                updateJobStatusSemaphore.release();
            }
        });
    }

    private void uploadProcessing() {
        var jobs = jobManagerService.takeForProcessing(
                OutboxJobType.UPLOAD,
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

                var results = documentManagerService.uploadDocumentAsync(docs);

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
        Map<Long, OutboxJobStatus> itemsStatusMap = Collections.emptyMap();
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
                job.getId(),
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
}
