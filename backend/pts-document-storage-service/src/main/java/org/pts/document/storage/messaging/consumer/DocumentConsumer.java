package org.pts.document.storage.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.pts.document.storage.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class DocumentConsumer {

    @RabbitListener()
    public void consume(UUID documentId) {
        log.info("documentId");
    }
}
