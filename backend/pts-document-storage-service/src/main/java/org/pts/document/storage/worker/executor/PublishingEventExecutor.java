package org.pts.document.storage.worker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.config.kafka.KafkaProperties;
import org.pts.document.storage.messaging.dto.KafkaEvent;
import org.pts.document.storage.messaging.producer.EventProducer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PublishingEventExecutor {
    private final KafkaProperties kafkaProperties;

    private final EventProducer eventProducer;

    public <T> void execute(List<KafkaEvent<T>> kafkaEvents) {
        try {

            var resultSending = kafkaEvents.stream().map(event ->
                    eventProducer.send(
                        kafkaProperties.getDocumentEventsTopic().getName(),
                        event.eventId().toString(),
                        event)
            ).toList();

            CompletableFuture.allOf(
                    resultSending.toArray(new CompletableFuture[0])
            ).join();

        } catch (Exception e) {
            throw new RuntimeException("Error while processing publishing events", e);
        }
    }
}
