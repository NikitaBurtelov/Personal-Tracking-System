package org.pts.document.storage.model.enums;

import lombok.Getter;

@Getter
public enum RequestStatus {
    NEW("NEW"),
    PROCESSING("PROCESSING"),
    DONE("DONE"),
    FAILED("FAILED");

    private final String status;

    RequestStatus(String status) {
        this.status = status;
    }
}