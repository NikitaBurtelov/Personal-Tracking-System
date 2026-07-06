package org.pts.document.storage.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProducer {

    public void send(String message) {
        String routingKey = "";

    }
}
