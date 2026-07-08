package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.dto.JobExecutionResult;
import org.pts.document.storage.model.dto.JobItemExecutionResult;
import org.pts.document.storage.model.enums.JobStatus;
import org.pts.document.storage.model.enums.JobType;
import org.pts.document.storage.service.document.DocumentManagerService;
import org.pts.document.storage.service.dto.DocumentContext;
import org.pts.document.storage.service.dto.JobContext;
import org.pts.document.storage.service.dto.JobItemContext;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UploadDocumentExecutor {
    private final DocumentManagerService documentManagerService;
    private final JobManagerService jobManagerService;

    public List<JobExecutionResult> execute() {
        var jobs = jobManagerService.takeForProcessing(
                JobType.UPLOAD,
                JobStatus.NEW,
                10
        );

        try {
            return jobs.entrySet()
                    .stream()
                    .map(entry -> {
                        var items = entry.getValue();

                        var documents = documentManagerService.uploadDocumentsAsync(
                                        items.stream()
                                                .map(JobItemContext::documentId)
                                                .toList()
                                ).stream()
                                .collect(Collectors.toMap(
                                        DocumentContext::documentId,
                                        Function.identity()
                                ));

                        return JobExecutionResult.builder()
                                .eventId(entry.getKey().eventId())
                                .jobId(entry.getKey().jobId())
                                .items(items.stream()
                                        .map(item -> {
                                            var document = documents.get(item.documentId());

                                            return JobItemExecutionResult.builder()
                                                    .itemId(item.itemId())
                                                    .documentId(document.documentId())
                                                    .result(document.result())
                                                    .message(document.message())
                                                    .build();
                                        })
                                        .toList())
                                .build();
                    })
                    .toList();
        } catch (Exception e) {
            var jobsIds = jobs.keySet()
                    .stream()
                    .map(JobContext::jobId)
                    .toList();
            var itemsIds = jobs.values()
                    .stream()
                    .flatMap(List::stream)
                    .map(JobItemContext::itemId)
                    .toList();

            log.error("Failed to upload documents for tasks: {} ", jobsIds, e);

            markFailed(
                    jobsIds,
                    itemsIds
            );

            return Collections.emptyList();
        }
    }

    private void markFailed(List<Long> jobIds, List<Long> itemIds) {
        jobManagerService.updateJobAndItemStatus(jobIds, itemIds, JobStatus.FAILED);
    }
}

