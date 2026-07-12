package org.pts.document.storage.domain.enums;

import lombok.Getter;

/**
 * Status enum for document lifecycle.
 * Documents transition through states: NEW -> UPLOADING -> UPLOADED -> DONE
 */
@Getter
public enum DocumentStatus {
    NEW("NEW"),
    UPLOADING("UPLOADING"),
    UPLOADED("UPLOADED"),
    DONE("DONE"),
    FAILED("FAILED");

    private final String status;

    DocumentStatus(String status) {
        this.status = status;
    }
}

