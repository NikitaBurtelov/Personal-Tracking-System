package org.pts.document.storage.model.enums;

import lombok.Getter;

@Getter
public enum ProcessingStatus {
    NEW("NEW"),
    PROCESSING("PROCESSING"),
    DONE("DONE"),
    FAILED("FAILED");

    private final String status;

    ProcessingStatus(String status) {
        this.status = status;
    }
}

