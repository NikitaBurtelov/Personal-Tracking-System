package org.pts.document.storage.messaging.command;

import java.util.List;
import java.util.UUID;

public record UploadDocumentCommand(
        UUID workId,
        PayloadDocumentsUpload payload
) {
    public record PayloadDocumentsUpload(
            List<Document> documents
    ) {
        public record Document(
                String s3TempKey,
                String bucket
        ) {
        }
    }
}