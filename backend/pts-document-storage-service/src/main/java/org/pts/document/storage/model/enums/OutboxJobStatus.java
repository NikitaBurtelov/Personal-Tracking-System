package org.pts.document.storage.model.enums;

import lombok.Getter;

@Getter
public enum OutboxJobStatus {
    NEW("NEW"),
    PROCESSING("PROCESSING"),
    DONE("DONE"),
    FAILED("FAILED");

    private final String status;

    private OutboxJobStatus(String status) {
        this.status = status;
    }
}