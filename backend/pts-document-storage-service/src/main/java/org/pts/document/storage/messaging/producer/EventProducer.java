package org.pts.document.storage.messaging.producer;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.messaging.dto.KafkaEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class EventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> CompletableFuture<SendResult<String, Object>> send(
            String topic,
            String key,
            KafkaEvent<T> event
    ) {

        return kafkaTemplate.executeInTransaction(operations ->
                operations.send(
                        topic,
                        key,
                        event
                )
        );
    }
}