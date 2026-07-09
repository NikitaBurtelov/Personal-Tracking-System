package org.pts.document.storage.messaging.command;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record UploadDocumentCommand(
        UUID workId,
        PayloadDocumentsUpload payload
) implements Command {
    @Builder
    public record PayloadDocumentsUpload(
            List<Document> documents
    ) {
        @Builder
        public record Document(
                String s3TempKey,
                String bucket
        ) {
        }
    }
}