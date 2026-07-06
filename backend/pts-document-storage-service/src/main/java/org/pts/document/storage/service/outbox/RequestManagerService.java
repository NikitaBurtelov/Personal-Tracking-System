package org.pts.document.storage.service.outbox;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface RequestManagerService {

    @Transactional
    void onJobCompleted(UUID requestId);
}
