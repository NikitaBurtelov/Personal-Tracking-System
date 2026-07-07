package org.pts.document.storage.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.pts.document.storage.model.entity.OutboxJobEntity;
import org.pts.document.storage.model.entity.OutboxJobItemEntity;
import org.pts.document.storage.service.dto.UploadResult;

import java.util.List;

@Builder
@Getter
@Setter
public class JobUploadData {
    private final OutboxJobEntity job;
    private final List<OutboxJobItemEntity> items;
    private final List<UploadResult> results;
}

