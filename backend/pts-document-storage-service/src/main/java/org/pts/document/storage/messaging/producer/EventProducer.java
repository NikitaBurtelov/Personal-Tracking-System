package org.pts.document.storage.messaging.producer;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.messaging.dto.KafkaEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void send(
            String topic,
            String key,
            KafkaEvent<T> event
    ) {

        kafkaTemplate.send(
                topic,
                key,
                event
        );
    }
}