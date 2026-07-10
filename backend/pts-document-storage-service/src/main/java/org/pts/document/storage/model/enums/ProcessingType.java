package org.pts.document.storage.model.enums;

import lombok.Getter;

@Getter
public enum ProcessingType {
    UPLOAD("UPLOAD"),
    GET("GET"),
    DELETE("DELETE");

    private final String type;

    ProcessingType(String type) {
        this.type = type;
    }
}

