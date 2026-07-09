package org.pts.document.storage.worker;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.JobExecutionResult;
import org.pts.document.storage.service.document.DocumentManagerService;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.pts.document.storage.worker.executor.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentWorker {
    private final DocumentManagerService documentManagerService;
    private final JobManagerService jobManagerService;

    private final ThreadPoolTaskExecutor threadPoolUploadDocumentProcessExecutor;
    private final ThreadPoolTaskExecutor threadPoolGetDocumentProcessExecutor;
    private final Executor virtualThreadPoolTaskProcessExecutor; // virtual thread

    private final DocumentMessageBuilderExecutor documentMessageBuilderExecutor;
    private final PublishingEventExecutor publishingEventExecutor;
    private final UploadDocumentExecutor uploadDocumentExecutor;
    private final GetDocumentExecutor getDocumentExecutor;
    private final UpdateJobStatusExecutor updateJobStatusExecutor;
    private final UpdateProcessingOperationStatusExecutor updateProcessingOperationStatusExecutor;

    private final Semaphore publicationEventProcessSemaphore;
    private final Semaphore uploadDocumentProcessSemaphore;
    private final Semaphore deleteDocumentProcessSemaphore;
    private final Semaphore getDocumentProcessSemaphore;
    private final Semaphore updateJobStatusProcessSemaphore;

    @Timed(
            value = "process.upload-document",
            percentiles = {
                    0.5,
                    0.95,
                    0.99
            }
    )
    @Scheduled(fixedDelay = 500)
    public void uploadDocumentProcess() {
        if (!uploadDocumentProcessSemaphore.tryAcquire()) {
            return;
        }

        threadPoolUploadDocumentProcessExecutor.execute(() -> {
            try {
                // Stage 1: upload documents
                var uploadResult = uploadDocumentExecutor.execute();

                if (uploadResult == null || uploadResult.isEmpty()) {
                    return;
                }

                // Stage 2: update statuses based on upload results
                updateJobStatusExecutor.execute(uploadResult);
                // Stage 3: build message
                var eventIds = uploadResult.stream().map(JobExecutionResult::eventId).toList();

                var message = documentMessageBuilderExecutor.buildUploadMessage(eventIds);
                if (message == null || message.isEmpty()) {
                    return;
                }
                // Stage 4: send message
                publishingEventExecutor.execute(message);
                // Stage 5: update event status
                updateProcessingOperationStatusExecutor.execute(eventIds);
            } catch (Exception e) {
                log.error("Error in uploadDocumentProcess: ", e);
            } finally {
                uploadDocumentProcessSemaphore.release();
            }
        });
    }

    @Timed(
            value = "process.get-document",
            percentiles = {
                    0.5,
                    0.95,
                    0.99
            }
    )
    @Scheduled(fixedDelay = 2000)
    public void getDocumentProcess() {

        if (!getDocumentProcessSemaphore.tryAcquire()) {
            return;
        }

        threadPoolGetDocumentProcessExecutor.execute(() -> {
            try {
                var getDocumentResult = getDocumentExecutor.execute();

                if (getDocumentResult == null || getDocumentResult.isEmpty()) {
                    return;
                }

                updateJobStatusExecutor.execute(getDocumentResult);

                var eventIds = getDocumentResult.stream().map(JobExecutionResult::eventId).toList();

                var message = documentMessageBuilderExecutor.buildGetMessage(eventIds);

                // Stage 4: send message
                publishingEventExecutor.execute(message);

                // Stage 5: update event status
                updateProcessingOperationStatusExecutor.execute(eventIds);
            } catch (Exception e) {
                log.error("Error in getDocumentProcess: ", e);
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

        virtualThreadPoolTaskProcessExecutor.execute(() -> {
            try {
                //TODO
            } finally {
                updateJobStatusProcessSemaphore.release();
            }
        });
    }
}
