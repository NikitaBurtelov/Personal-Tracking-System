package org.pts.document.storage.messaging.command;

import java.util.UUID;

public record DeleteDocumentCommand(
        UUID workId
) {
}
