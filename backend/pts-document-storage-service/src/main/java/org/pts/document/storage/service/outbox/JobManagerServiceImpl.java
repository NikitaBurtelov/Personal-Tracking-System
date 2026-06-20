package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.model.entity.DocumentEntity;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.model.enums.DocumentStatus;
import org.pts.document.storage.model.enums.OutboxJobStatus;
import org.pts.document.storage.model.enums.OutboxJobType;
import org.pts.document.storage.repository.DocumentRepository;
import org.pts.document.storage.repository.OutboxItemRepository;
import org.pts.document.storage.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobManagerServiceImpl implements JobManagerService {
    private final DocumentRepository documentRepository;
    private final OutboxRepository outboxRepository;
    private final OutboxItemRepository outboxItemRepository;

    @Transactional
    @Override
    public void createUploadDocumentJob(UploadDocumentCommand msg) {
        var job = OutboxJobEntity.builder()
                .type(OutboxJobType.UPLOAD)
                .status(OutboxJobStatus.NEW)
                .createdAt(Instant.now())
                .updateAt(Instant.now())
                .build();

        outboxRepository.save(job);

        var documents = new ArrayList<DocumentEntity>();
        var jobItems = new ArrayList<OutboxJobItemEntity>();

        for (var doc : msg.payload().documents()) {

            var documentId = UUID.randomUUID();

            var document = DocumentEntity.builder()
                    .id(documentId)
                    .tempKey(doc.s3TempKey())
                    .tempBucket(doc.bucket())
                    .status(DocumentStatus.NEW)
                    .build();

            documents.add(document);

            documentRepository.save(document);

            var jobItem = OutboxJobItemEntity.builder()
                    .jobId(job.getId())
                    .documentId(documentId)
                    .status(OutboxJobStatus.NEW)
                    .build();

            jobItems.add(jobItem);

        }

        documentRepository.saveAll(documents);
        outboxItemRepository.saveAll(jobItems);
    }

    @Transactional
    @Override
    public Map<OutboxJobEntity, List<OutboxJobItemEntity>> takeForProcessing(
            OutboxJobType type,
            OutboxJobStatus status,
            int limit
    ) {
        var jobs = outboxRepository.findAllByTypeAndStatus(type.getType(), status.getStatus());

        jobs.forEach(job -> job.setStatus(OutboxJobStatus.PROCESSING));

        var items = outboxItemRepository
                .findAllByJobIdIn(
                        jobs.stream()
                                .map(OutboxJobEntity::getId)
                                .toList()
                );

        items.forEach(item -> item.setStatus(OutboxJobStatus.PROCESSING));

        Map<Long, List<OutboxJobItemEntity>> idsItemsMap = items
                .stream()
                .collect(Collectors.groupingBy(OutboxJobItemEntity::getJobId));

        return jobs.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        job -> idsItemsMap.getOrDefault(job.getId(), List.of())
                ));
    }

    @Transactional
    @Override
    public void updateJobAndItemStatus(
            Long jobId,
            OutboxJobStatus jobStatus,
            Map<Long, OutboxJobStatus> itemsStatusMap
    ) {
        outboxRepository.updateStatus(jobId, jobStatus);

        Map<OutboxJobStatus, List<Long>> grouped = itemsStatusMap.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));


        grouped.forEach((groupedStatus, ids) -> {
            outboxItemRepository.updateStatus(ids, groupedStatus);
        });
    }

    @Transactional
    @Override
    public void updateJobAndItemStatus(
            Long jobId,
            List<Long> itemsId,
            OutboxJobStatus status
    ) {
        var updatedOutboxField = outboxRepository.updateStatus(jobId, status);

        if (updatedOutboxField == 0) {
            throw new IllegalStateException(
                    "Failed to update outbox job. jobId=" + jobId
            );
        }

        var updatedItemField = outboxItemRepository.updateStatus(itemsId, status);

        if (updatedItemField != itemsId.size()) {
            throw new IllegalStateException(
                    "Expected to update " + itemsId.size() +
                            " outbox items but updated " + updatedItemField
            );
        }
    }
}
