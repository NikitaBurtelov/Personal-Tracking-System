package org.pts.document.storage.service.dto;

import lombok.Builder;
import org.pts.document.storage.model.enums.JobType;

import java.util.List;
import java.util.UUID;

@Builder
public record JobContext(
        Long jobId,
        UUID eventId,
        JobType type,
        List<JobItemContext> items
) {
}
