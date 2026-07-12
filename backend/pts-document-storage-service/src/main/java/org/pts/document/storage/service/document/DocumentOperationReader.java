package org.pts.document.storage.service.document;

import org.pts.document.storage.model.entity.DocumentEntity;

import java.util.List;
import java.util.UUID;

public interface DocumentOperationReader {
    /**
     * Retrieves all documents associated with a given processing request ID.
     *
     * @param operationId the processing request ID
     * @return list of DocumentEntity objects associated with the request
     */
    List<DocumentEntity> getDocumentsByOperationId(UUID operationId);
}