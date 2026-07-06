package org.pts.document.storage.worker.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.OutboxJobStatus;
import org.pts.document.storage.model.enums.OutboxJobType;
import org.pts.document.storage.service.DocumentManagerService;
import org.pts.document.storage.service.dto.UploadResult;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadProcess {
    private final DocumentManagerService documentManagerService;
    private final JobManagerService jobManagerService;

    public Map<Long, List<UploadResult>> execute() {
        var jobs = jobManagerService.takeForProcessing(
                OutboxJobType.UPLOAD,
                OutboxJobStatus.NEW,
                10
        );

        var jobsId = jobs.keySet().stream().map(OutboxJobEntity::getId).toList();
        HashMap<Long, List<UploadResult>> result = HashMap.newHashMap(jobs.size());

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


                result.put(job.getId(), documentManagerService.uploadDocumentAsync(docs));
            });

            return  result;
        } catch (Exception e) {
            log.error("Failed to complete tasks: {} ", jobsId, e);
            throw new RuntimeException(e);
        }
    }
}
