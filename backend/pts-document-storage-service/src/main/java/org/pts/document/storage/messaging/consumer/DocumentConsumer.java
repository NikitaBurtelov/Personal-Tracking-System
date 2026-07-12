package org.pts.document.storage.messaging.consumer;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.domain.processing.ProcessingOperationManager;
import org.pts.document.storage.messaging.command.DeleteDocumentCommand;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.messaging.dto.GetDocumentSourceRequest;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@KafkaListener(
        topics = "${app.kafka.topic.document-commands-topic.name}",
        groupId = "${spring.kafka.consumer.group-id}",
        concurrency = "${spring.kafka.listener.concurrency}"
)
@RequiredArgsConstructor
@Slf4j
public class DocumentConsumer {
    private final ProcessingOperationManager processingOperationManager;

    @Timed(
            value = "messaging.document.upload-command",
            percentiles = {
                    0.5,
                    0.95,
                    0.99
            }
    )
    @KafkaHandler
    public void uploadDocumentSource(
            @Payload UploadDocumentCommand message
    ) throws IOException {
        try {
            log.info("A request to upload files has been received. workId:{}", message.workId());
            processingOperationManager.createUploadDocumentTask(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaHandler
    public void getDocumentSource(GetDocumentSourceRequest message) {
        try {
            log.info("Request to view files received. workId:{}", message.workId());
            processingOperationManager.createGetDocumentTask(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaHandler
    public void deleteDocument(DeleteDocumentCommand message) {
        try {
            log.info("A request to delete files was received. workdId={}", message.workId());
            //TODO jobManagerService.createDeleteDocumentJob(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}