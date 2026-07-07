package org.pts.document.storage.model.enums;

import lombok.Getter;

@Getter
public enum Status {
    NEW("NEW"),
    PROCESSING("PROCESSING"),
    UPLOADING("UPLOADING"),
    UPLOADED("UPLOADING"),
    DONE("DONE"),
    FAILED("FAILED");

    private final String status;

    Status(String status) {
        this.status = status;
    }
}