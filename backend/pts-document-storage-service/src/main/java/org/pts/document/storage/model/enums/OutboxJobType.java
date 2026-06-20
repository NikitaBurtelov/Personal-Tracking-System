package org.pts.document.storage.model.enums;

import lombok.Getter;

@Getter
public enum OutboxJobType {
    UPLOAD("UPLOAD"),
    GET("GET");

    private final String type;

    private OutboxJobType(String type) {
        this.type = type;
    }
}