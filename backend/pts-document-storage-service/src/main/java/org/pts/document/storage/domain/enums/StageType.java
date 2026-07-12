package org.pts.document.storage.domain.enums;

import lombok.Getter;

@Getter
public enum StageType {
    UPLOAD_DOCUMENTS("UPLOAD_DOCUMENTS"),
    UPDATE_STATUS("UPDATE_STATUS"),
    CREATE_EVENT("CREATE_EVENT"),
    PUBLISH_EVENT("UPLOADED"),
    DELETE_DOCUMENTS("DELETE");

    private final String type;

    StageType(String type) {
        this.type = type;
    }
}

