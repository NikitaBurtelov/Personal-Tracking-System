package org.pts.document.storage.model.enums;

import lombok.Getter;

@Getter
public enum Type {
    DONE("DONE"),
    UPLOAD("UPLOAD"),
    GET("GET"),
    DELETE("DELETE");

    private final String type;

    Type(String type) {
        this.type = type;
    }
}