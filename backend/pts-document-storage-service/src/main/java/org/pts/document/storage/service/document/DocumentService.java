package org.pts.document.storage.service.document;

import java.util.UUID;

public interface DocumentService {
    String getDocument(String key, String bucket) throws Exception;

    String upload(UUID id) throws Exception;
}
