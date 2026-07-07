package org.pts.document.storage.model.enums;

import lombok.Getter;

/**
 * Type enum for outbox job operations.
 */
@Getter
public enum JobType {
    UPLOAD("UPLOAD"),
    GET("GET"),
    DELETE("DELETE");

    private final String type;

    JobType(String type) {
        this.type = type;
    }
}

