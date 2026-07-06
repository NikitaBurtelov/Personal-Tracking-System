package org.pts.document.storage.model.enums;

import lombok.Getter;

@Getter
public enum RequestType {
    UPLOAD("UPLOAD"),
    GET("GET"),
    DELETE("DELETE");

    private final String type;

    RequestType(String type) {
        this.type = type;
    }
}