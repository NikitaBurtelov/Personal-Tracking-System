package org.pts.document.storage.service.dto;

import lombok.Builder;
import org.pts.document.storage.model.enums.JobStatus;

import java.util.UUID;

@Builder
public record JobItemContext(
        Long itemId,
        UUID documentId,
        Long jobId,
        JobStatus status
) {
}