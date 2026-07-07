package org.pts.document.storage.model.dto;

import java.util.List;

/**
 * Result of upload processing stage.
 * Contains a list of job upload data, each with job, items, and their upload results.
 */
public record UploadProcessResult(List<JobUploadData> jobsData) {
}

