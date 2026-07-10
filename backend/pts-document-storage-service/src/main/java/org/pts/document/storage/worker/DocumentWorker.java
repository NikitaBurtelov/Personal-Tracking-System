package org.pts.document.storage.worker;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.BatchExecutionResult;
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
    private final ThreadPoolTaskExecutor threadPoolUploadDocumentProcessExecutor;
    private final ThreadPoolTaskExecutor threadPoolGetDocumentProcessExecutor;
    private final Executor virtualThreadPoolTaskProcessExecutor; // virtual thread

    private final EventMessageBuilder eventMessageBuilder;
    private final OperationEventsFinder  operationEventsFinder;

    private final PublishingEventExecutor publishingEventExecutor;
    private final UploadDocumentExecutor uploadDocumentExecutor;
    private final GetDocumentExecutor getDocumentExecutor;
    private final UpdateTaskStatusExecutor updateTaskStatusExecutor;
    private final UpdateEventStatusExecutor updateEventStatusExecutor;

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

                if (uploadResult.isEmpty()) {
                    return;
                }

                // Stage 2: update statuses based on upload results
                updateTaskStatusExecutor.execute(uploadResult);

                var operationsIds = uploadResult.stream().map(BatchExecutionResult::operationId).toList();

                var eventsIds = operationEventsFinder.findEventByCompletedOperation(operationsIds);

                if (eventsIds.isEmpty()) {
                    return;
                }

                // Stage 3: build message
                var message = eventMessageBuilder.buildUploadMessage(eventsIds);
                if (message == null || message.isEmpty()) {
                    return;
                }
                // Stage 4: send message
                publishingEventExecutor.execute(message);
                // Stage 5: update event status
                updateEventStatusExecutor.execute(eventsIds);
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

                var qq = updateTaskStatusExecutor.execute(getDocumentResult);

                var eventIds = getDocumentResult.stream().map(BatchExecutionResult::operationId).toList();

                var message = eventMessageBuilder.buildGetMessage(eventIds);

                // Stage 4: send message
                publishingEventExecutor.execute(message);

                // Stage 5: update event status
                updateEventStatusExecutor.execute(eventIds);
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
