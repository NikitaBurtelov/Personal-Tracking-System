package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.BatchExecutionResult;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.pts.document.storage.service.processing.ProcessingOperationManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateTaskStatusExecutor {
    private final ProcessingOperationManager processingOperationManager;

    public void execute(List<BatchExecutionResult> batchExecutionResults) {
        if (batchExecutionResults == null || batchExecutionResults.isEmpty()) {
            return;
        }

        try {
            Map<Long, ProcessingStatus> batchStatusMap = new HashMap<>();
            Map<Long, ProcessingStatus> taskStatusMap = new HashMap<>();

            for (var result : batchExecutionResults) {
                AtomicReference<ProcessingStatus> batchStatus = new AtomicReference<>(ProcessingStatus.DONE);

                result.taskExecutionResults().forEach(item -> {
                    if (item.result() == null) {
                        batchStatus.set(ProcessingStatus.FAILED);
                        taskStatusMap.put(item.taskId(), ProcessingStatus.FAILED);
                    } else {
                        taskStatusMap.put(item.taskId(), ProcessingStatus.DONE);
                    }
                });

                batchStatusMap.put(result.batchId(), batchStatus.get());
            }

            processingOperationManager.updateBatchAndTaskStatus(
                    batchStatusMap,
                    taskStatusMap
            );
        } catch (Exception e) {
            log.error("Failed to update statuses", e);
        }
    }
}
