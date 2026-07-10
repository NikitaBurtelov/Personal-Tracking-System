package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.config.minio.MinIOProperties;
import org.pts.document.storage.messaging.dto.DocumentDataPayload;
import org.pts.document.storage.messaging.dto.KafkaEvent;
import org.pts.document.storage.service.document.DocumentOperationManager;
import org.pts.document.storage.service.outbox.EventManagerService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventMessageBuilder {
    private final MinIOProperties minIOProperties;

    private final EventManagerService eventManagerService;
    private final DocumentOperationManager documentOperationManager;

    public List<KafkaEvent<DocumentDataPayload>> buildUploadMessage(List<UUID> eventIds) {
        var events = eventManagerService.getUnpublishedEvents(eventIds);

        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }

        return events.stream()
                .map(event -> {
                    var documents = documentOperationManager.getDocumentsByOperationId(event.getOperationId());
                    var bucket = minIOProperties.getDocumentPersistenceBucket().getBucketName();
                    return KafkaEvent.of(
                            "DOCUMENT_UPLOAD_COMPLETED",
                            event.getOperationId().toString(),
                            DocumentDataPayload.builder()
                                    .workId(event.getOperationId())
                                    .bucket(bucket)
                                    .documentData(
                                            documents.stream()
                                                    .map(doc ->
                                                            DocumentDataPayload.DocumentData.builder()
                                                                    .s3Key(doc.getObjectKey())
                                                                    .id(doc.getId())
                                                                    .build()
                                                    )
                                                    .toList()
                                    )
                                    .build()
                    );
                }).toList();
    }

    public List<KafkaEvent<DocumentDataPayload>> buildGetMessage(List<UUID> eventIds) {
        var events = eventManagerService.getUnpublishedEvents(eventIds);

        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }

        return events.stream()
                .map(event -> {
                    var documents = documentOperationManager.getDocumentsByOperationId(event.getOperationId());
                    var bucket = minIOProperties.getDocumentTempBucket().getBucketName();
                    return KafkaEvent.of(
                            "DOCUMENT_SENDED_COMPLETED",
                            event.getOperationId().toString(),
                            DocumentDataPayload.builder()
                                    .workId(event.getOperationId())
                                    .bucket(bucket)
                                    .documentData(
                                            documents.stream()
                                                    .map(doc ->
                                                            DocumentDataPayload.DocumentData.builder()
                                                                    .s3Key(doc.getTransferObjectKey())
                                                                    .id(doc.getId())
                                                                    .build()
                                                    )
                                                    .toList()
                                    )
                                    .build()
                    );
                }).toList();
    }

    public List<KafkaEvent<DocumentDataPayload>> buildDeleteMessage(List<UUID> eventIds) {
        //TODO
        return Collections.emptyList();
    }
}
