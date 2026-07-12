package org.pts.document.storage.service.dto;

import lombok.Builder;
import lombok.Data;
import org.pts.document.storage.model.enums.ProcessingStatus;
import org.pts.document.storage.model.enums.ProcessingType;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BatchContext {
    Long batchId;
    UUID operationId;
    ProcessingStatus processingStatus;
    ProcessingType type;
    List<TaskContext> taskContexts;
}
