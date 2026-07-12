package org.pts.document.storage.domain.enums;

import lombok.Getter;

@Getter
public enum ProcessingStatus {
    NEW("NEW"),
    PROCESSING("PROCESSING"),
    DOCUMENTS_UPLOADED("DOCUMENTS_UPLOADED"),
    UPDATED_STATUS("UPDATED_STATUS"),
    OPERATION_COMPLETED("CREATED_EVENT"),
    CREATED_EVENT("CREATED_EVENT"),
    PUBLISHED_EVENT("PUBLISHED_EVENT"),
    DELETE_DOCUMENTS("DELETE"),
    DONE("DONE"),
    FAILED("FAILED");

    private final String status;

    ProcessingStatus(String status) {
        this.status = status;
    }
}

