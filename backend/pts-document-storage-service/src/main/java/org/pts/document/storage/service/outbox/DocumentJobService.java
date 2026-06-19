package org.pts.document.storage.service.outbox;

import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.springframework.transaction.annotation.Transactional;

public interface DocumentJobService {
    @Transactional
    void createUploadDocumentJob(UploadDocumentCommand msg);
}
