package org.pts.document.storage.model.enums;

import lombok.Getter;

@Getter
public enum DocumentStatus {
    NEW("NEW"),
    UPLOADING("UPLOADING"),
    UPLOADED("UPLOADED"),
    FAILED("FAILED");

    private final String status;

    private DocumentStatus(String status) {
        this.status = status;
    }
}
