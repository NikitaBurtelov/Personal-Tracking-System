package org.pts.document.storage.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class DocumentConsumer {

    @RabbitListener(queues = "#{documentStorageQueue.name}")
    public void consume() {
        log.info("documentId");
    }
}
