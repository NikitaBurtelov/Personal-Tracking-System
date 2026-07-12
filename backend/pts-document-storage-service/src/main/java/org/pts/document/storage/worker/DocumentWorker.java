package org.pts.document.storage.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.domain.enums.ProcessingStatus;
import org.pts.document.storage.domain.enums.ProcessingType;
import org.pts.document.storage.domain.context.BatchContext;
import org.pts.document.storage.worker.executor.*;
import org.pts.document.storage.worker.support.ProcessingActions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentWorker {
    private final ThreadPoolTaskExecutor threadPoolUploadDocumentProcessExecutor;
    private final ThreadPoolTaskExecutor threadPoolGetDocumentProcessExecutor;
    private final Executor virtualThreadPoolTaskProcessExecutor; // virtual thread

    private final ProcessingBatchProvider processingBatchProvider;
    private final EventMessageBuilder eventMessageBuilder;
    private final OperationEventsFinder operationEventsFinder;

    private final CreateEventExecutor createEventExecutor;
    private final BatchCompletionExecutor batchCompletionExecutor;
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

    @Scheduled(fixedDelay = 500)
    public void uploadDocumentProcess() {
        if (!uploadDocumentProcessSemaphore.tryAcquire()) {
            return;
        }

        threadPoolUploadDocumentProcessExecutor.execute(() -> {
            try {
                //Stage 1: get batch
                final var batchesGroupedByStatus = processingBatchProvider.take(
                        ProcessingType.UPLOAD,
                        10
                );

                if (batchesGroupedByStatus.isEmpty())
                    return;

                //Stage 2.1: upload document
                var uploadResult = ProcessingActions.execute(
                        () -> uploadDocumentExecutor.execute(
                                Stream.of(ProcessingStatus.NEW)
                                        .map(batchesGroupedByStatus::get)
                                        .filter(Objects::nonNull)
                                        .flatMap(List::stream)
                                        .toList()
                        )
                );

                // Stage 2.2: update status
                ProcessingActions.executeAndRegroup(
                        batchesGroupedByStatus,
                        () -> updateTaskStatusExecutor.execute(
                                batchesGroupedByStatus.get(ProcessingStatus.NEW),
                                uploadResult
                        )
                );

                // Stage 3: обновить operation и batch
                ProcessingActions.executeAndRegroup(
                        batchesGroupedByStatus,
                        () -> batchCompletionExecutor.execute(
                                batchesGroupedByStatus.get(ProcessingStatus.DOCUMENTS_UPLOADED)
                        )
                );

                ProcessingActions.executeAndRegroup(
                        batchesGroupedByStatus,
                        () -> createEventExecutor.execute(
                                batchesGroupedByStatus.get(ProcessingStatus.OPERATION_COMPLETED)
                        )
                );

                //Stage 4.1: find event
                var eventsIds = operationEventsFinder.findEventByCompletedOperation(
                        batchesGroupedByStatus.get(ProcessingStatus.CREATED_EVENT)
                );

                if (!eventsIds.isEmpty()) {
                    //Stage 4.2 build message
                    var message = eventMessageBuilder.buildUploadMessage(eventsIds);
                    // Stage 4.3: send message
                    ProcessingActions.execute(
                            () -> publishingEventExecutor.execute(message)
                    );
                }

                // Stage 5: update event status=Done and batch status=Done
                ProcessingActions.executeAndRegroup(
                        batchesGroupedByStatus,
                        () -> updateEventStatusExecutor.execute(
                                batchesGroupedByStatus.get(ProcessingStatus.CREATED_EVENT),
                                eventsIds
                        )
                );
            } catch (Exception e) {
                log.error("Error in uploadDocumentProcess: ", e);
            } finally {
                uploadDocumentProcessSemaphore.release();
            }
        });
    }

//    @Timed(
//            value = "process.get-document",
//            percentiles = {
//                    0.5,
//                    0.95,
//                    0.99
//            }
//    )
//    @Scheduled(fixedDelay = 2000)
//    public void getDocumentProcess() {
//
//        if (!getDocumentProcessSemaphore.tryAcquire()) {
//            return;
//        }
//
//        threadPoolGetDocumentProcessExecutor.execute(() -> {
//            try {
//                var getDocumentResult = getDocumentExecutor.execute();
//
//                if (getDocumentResult == null || getDocumentResult.isEmpty()) {
//                    return;
//                }
//
//                updateTaskStatusExecutor.execute(getDocumentResult);
//
//                var eventIds = getDocumentResult.stream().map(BatchExecutionResult::operationId).toList();
//
//                var message = eventMessageBuilder.buildGetMessage(eventIds);
//
//                // Stage 4: send message
//                publishingEventExecutor.execute(message);
//
//                // Stage 5: update event status
//                updateEventStatusExecutor.execute(eventIds);
//            } catch (Exception e) {
//                log.error("Error in getDocumentProcess: ", e);
//            } finally {
//                getDocumentProcessSemaphore.release();
//            }
//        });
//    }

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

    private Map<ProcessingStatus, List<BatchContext>> regroup(
            Map<ProcessingStatus, List<BatchContext>> batchesGroupedByStatus
    ) {
        return batchesGroupedByStatus.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(
                        BatchContext::getProcessingStatus,
                        () -> new EnumMap<>(ProcessingStatus.class),
                        Collectors.toList()
                ));
    }
}
