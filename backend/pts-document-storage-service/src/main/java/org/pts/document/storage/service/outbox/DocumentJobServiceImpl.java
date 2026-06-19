package org.pts.document.storage.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.model.DocumentEntity;
import org.pts.document.storage.model.OutboxJobEntity;
import org.pts.document.storage.model.OutboxJobItemEntity;
import org.pts.document.storage.repository.DocumentRepository;
import org.pts.document.storage.repository.OutboxItemRepository;
import org.pts.document.storage.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentJobServiceImpl implements DocumentJobService {
    private final DocumentRepository documentRepository;
    private final OutboxRepository outboxRepository;
    private final OutboxItemRepository outboxItemRepository;

    @Transactional
    @Override
    public void createUploadDocumentJob(UploadDocumentCommand msg) {
        var job = OutboxJobEntity.builder()
                .type("UPLOAD")
                .status("NEW")
                .createdAt(OffsetDateTime.now())
                .updateAt(OffsetDateTime.now())
                .build();

        outboxRepository.save(job);

        for (var doc : msg.payload().documents()) {

            var documentId = UUID.randomUUID();

            var document = DocumentEntity.builder()
                    .id(documentId)
                    .tempKey(doc.s3TempKey())
                    .tempBucket(doc.bucket())
                    .status("PENDING")
                    .build();

            documentRepository.save(document);

            var jobItem = OutboxJobItemEntity.builder()
                    .jobId(job.getId())
                    .documentId(documentId)
                    .status("NEW")
                    .build();

            outboxItemRepository.save(jobItem);
        }
    }
}
