package org.pts.document.storage.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record KafkaEvent<T>(
        UUID eventId,
        String eventType,
        Instant timestamp,
        T payload
) {

    public static <T> KafkaEvent<T> of(
            String eventType,
            String eventId,
            T payload
    ) {
        return new KafkaEvent<>(
                UUID.fromString(eventId),
                eventType,
                Instant.now(),
                payload
        );
    }
}
