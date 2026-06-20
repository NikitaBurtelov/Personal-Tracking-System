package org.pts.document.storage.messaging.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.messaging.command.DeleteDocumentCommand;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentConsumer {
    private final JobManagerService jobManagerService;

    @RabbitListener(queues = "document-storage.request.get.queue")
    public void getDocumentSource(GetDocumentSourceRequest message) {
        log.info("message={}", message);
    }

    @RabbitListener(queues = "document-storage.command.upload.queue")
    public void uploadDocumentSource(
            UploadDocumentCommand message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        try {

            jobManagerService.createUploadDocumentJob(message);

            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicNack(tag, false, true);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "document-storage.command.queue")
    public void deleteDocument(DeleteDocumentCommand message) {
        log.info("message={}", message);
    }
}