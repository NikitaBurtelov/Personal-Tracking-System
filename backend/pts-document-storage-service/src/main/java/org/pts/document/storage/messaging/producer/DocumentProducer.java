package org.pts.document.storage.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.config.mq.RabbitProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProducer {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties properties;

    public void send(String message) {
        String exchange = properties.getExchange().getName();
        String routingKey = "";

        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                message
        );
    }
}
