package org.pts.document.storage.model.enums;

import lombok.Getter;

/**
 * Status enum for outbox job and item lifecycle.
 * Jobs/Items transition through states: NEW -> PROCESSING -> DONE
 */
@Getter
public enum JobStatus {
    NEW("NEW"),
    PROCESSING("PROCESSING"),
    DONE("DONE"),
    FAILED("FAILED");

    private final String status;

    JobStatus(String status) {
        this.status = status;
    }
}

