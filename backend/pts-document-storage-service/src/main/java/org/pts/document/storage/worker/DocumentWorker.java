package org.pts.document.storage.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.OutboxJobEntity;
import org.pts.document.storage.model.OutboxJobItemEntity;
import org.pts.document.storage.service.DocumentManagerService;
import org.pts.document.storage.service.dto.UploadResult;
import org.pts.document.storage.service.outbox.OutboxItemRepositoryService;
import org.pts.document.storage.service.outbox.OutboxRepositoryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentWorker {
    private final OutboxRepositoryService outboxRepositoryService;
    private final OutboxItemRepositoryService outboxItemRepositoryService;
    private final DocumentManagerService documentManagerService;

    @Scheduled(fixedDelay = 3000)
    public void process() {

        var jobs = outboxRepositoryService.findByStatus("NEW");

        for (OutboxJobEntity job : jobs) {
            var items = outboxItemRepositoryService.findByJobId(job.getId())
                    .stream()
                    .filter(item -> item.getStatus().equals("NEW") || item.getStatus().equals("FAILED"))
                    .toList();

            try {
                var docs = items.stream().map(OutboxJobItemEntity::getDocumentId).toList();

                var itemsDocsMap = items.stream()
                        .collect(
                                Collectors.toMap(
                                        OutboxJobItemEntity::getDocumentId,
                                        item -> item)
                        );

                markProcessing(job, items);

                var results = documentManagerService.uploadDocumentAsync(docs);

                markDone(job, itemsDocsMap, results);

            } catch (Exception e) {
                markFailed(job, items);
            }
        }
    }

    private void markProcessing(OutboxJobEntity job, List<OutboxJobItemEntity> items) {
        job.setStatus("PROCESSING");
        job.setUpdateAt(OffsetDateTime.now());

        items.forEach(this::markItemProcessing);

        outboxRepositoryService.save(job);
    }

    private void markDone(OutboxJobEntity job, Map<UUID, OutboxJobItemEntity> itemsDocsMap, List<UploadResult> results) {
        AtomicBoolean isDone = new AtomicBoolean(true);
        
        results.forEach(result -> {
            var item = itemsDocsMap.get(result.docId());

            if (result.result() == null) {
                isDone.set(false);
                markItemFailed(item);
            } else {
                markItemDone(item);
            }
        });

        if (isDone.get()) {
            job.setStatus("DONE");
            job.setUpdateAt(OffsetDateTime.now());
        } else {
            job.setStatus("FAILED");
            job.setUpdateAt(OffsetDateTime.now());
        }

        outboxRepositoryService.save(job);
    }

    private void markFailed(OutboxJobEntity job, List<OutboxJobItemEntity> items) {
        job.setStatus("FAILED");
        job.setUpdateAt(OffsetDateTime.now());

        items.forEach(this::markItemFailed);

        outboxRepositoryService.save(job);
    }

    private void markItemProcessing(OutboxJobItemEntity item) {
        item.setStatus("PROCESSING");
        outboxItemRepositoryService.save(item);
    }

    private void markItemDone(OutboxJobItemEntity item) {
        item.setStatus("DONE");
        outboxItemRepositoryService.save(item);
    }

    private void markItemFailed(OutboxJobItemEntity item) {
        item.setStatus("FAILED");
        outboxItemRepositoryService.save(item);
    }
}
