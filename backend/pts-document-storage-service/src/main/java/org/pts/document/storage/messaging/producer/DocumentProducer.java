package org.pts.document.storage.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.pts.document.storage.config.properties.RabbitProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class DocumentProducer {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties properties;

    public void send(String message) {
        String exchange = properties.getExchange().getName();
        String routingKey = properties.getDocumentStorageBinding().getRoutingKey();

        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
