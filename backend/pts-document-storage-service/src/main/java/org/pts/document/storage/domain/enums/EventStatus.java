package org.pts.document.storage.domain.enums;

import lombok.Getter;

/**
 * Status enum for outbox event lifecycle.
 * Events transition through states: NEW -> PUBLISHING -> PUBLISHED
 */
@Getter
public enum EventStatus {
    NEW("NEW"),
    PUBLISHING("PUBLISHING"),
    PUBLISHED("PUBLISHED"),
    FAILED("FAILED");

    private final String status;

    EventStatus(String status) {
        this.status = status;
    }
}

