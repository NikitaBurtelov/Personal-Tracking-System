package org.pts.document.storage.worker.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.config.kafka.KafkaProperties;
import org.pts.document.storage.config.minio.MinIOProperties;
import org.pts.document.storage.messaging.dto.DocumentDataPayload;
import org.pts.document.storage.messaging.dto.KafkaEvent;
import org.pts.document.storage.messaging.producer.EventProducer;
import org.pts.document.storage.model.entity.OutboxEventEntity;
import org.pts.document.storage.service.document.DocumentJobManager;
import org.pts.document.storage.service.outbox.EventManagerService;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PublishingEventProcessing {
    private final MinIOProperties properties;
    private final KafkaProperties kafkaProperties;

    private final EventManagerService eventManagerService;
    private final DocumentJobManager documentJobManager;

    private final EventProducer eventProducer;

    public void execute() {
        try {
            var events = eventManagerService.getUnpublishedEvents(10);

            if (events.isEmpty()) {
                return;
            }

            var resultSending = events.stream()
                    .map(event -> {
                        var documents = documentJobManager.getDocumentsByRequestId(event.getRequestId());
                        var bucket = properties.getDocumentPersistenceBucket().getBucketName();

                        var response = KafkaEvent.of(
                                "eventType",
                                event.getRequestId().toString(),
                                DocumentDataPayload.builder()
                                        .workId(event.getRequestId())
                                        .bucket(bucket)
                                        .documentData(
                                                documents.stream()
                                                        .map(doc ->
                                                                DocumentDataPayload.DocumentData.builder()
                                                                        .s3Key(doc.getKey())
                                                                        .id(doc.getId())
                                                                        .build()
                                                        )
                                                        .toList()
                                        )
                                        .build()
                        );

                        return eventProducer.send(
                                kafkaProperties.getDocumentEventsTopic().getName(),
                                event.getRequestId().toString(),
                                response
                        );
                    }).toList();

            CompletableFuture.allOf(
                    resultSending.toArray(new CompletableFuture[0])
            ).join();

            eventManagerService.markEventsAsPublished(events);

            log.info("Successfully processed publishing events, requestIds: {}",
                    events.stream().map(OutboxEventEntity::getRequestId).toList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error while processing publishing events", e);
        }
    }
}
