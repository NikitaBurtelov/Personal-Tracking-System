package org.pts.document.storage.worker.process;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.messaging.dto.DocumentDataPayload;
import org.pts.document.storage.messaging.dto.KafkaEvent;
import org.pts.document.storage.messaging.producer.EventProducer;
import org.pts.document.storage.service.outbox.EventManagerService;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishingEventProcessing {
    private final EventManagerService eventManagerService;
    private final JobManagerService jobManagerService;
    private final EventProducer eventProducer;

    public void execute() {
        var events = eventManagerService.getUnpublishedEvents(10);

        events.forEach(event -> {
            var documents = jobManagerService.getDocument(event.getRequestId());

            var response = KafkaEvent.of(
                    "eventType",
                    event.getRequestId().toString(),
                    DocumentDataPayload.builder()
                            .bucket(documents.getBucket())
                            .temps3Keys(documents.getTemps3Keys())
                            .build()
            );

            eventProducer.send("topic", event.getRequestId().toString(), documents);
        });


    }
}
