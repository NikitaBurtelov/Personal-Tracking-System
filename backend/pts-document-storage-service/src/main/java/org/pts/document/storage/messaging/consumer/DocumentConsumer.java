package org.pts.document.storage.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.messaging.command.DeleteDocumentCommand;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "${rabbit.document-source-commands-queue.name}")
@RequiredArgsConstructor
@Slf4j
public class DocumentConsumer {
    private final JobManagerService jobManagerService;

    @RabbitHandler
    public void getDocumentSource(GetDocumentSourceRequest message) {
        try {
            log.info("Request to view files received. workId:{}", message.workId());
            jobManagerService.createGetDocumentJob(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitHandler
    public void uploadDocumentSource(
            UploadDocumentCommand message
    ) throws IOException {
        try {
            log.info("A request to upload files has been received. workId:{}", message.workId());
            jobManagerService.createUploadDocumentJob(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitHandler
    public void deleteDocument(DeleteDocumentCommand message) {
        try {
            log.info("A request to delete files was received. workdId={}", message.workId());
            //TODO jobManagerService.createDeleteDocumentJob(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}