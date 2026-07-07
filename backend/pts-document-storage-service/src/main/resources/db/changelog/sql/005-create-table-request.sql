set search_path to document_storage_schema;

CREATE TABLE IF NOT EXISTS processing_request
(
    id             UUID NOT NULL,
    type           VARCHAR(255),
    status         VARCHAR(255),
    total_jobs     INTEGER,
    completed_jobs INTEGER,
    created_at     TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    completed_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_processing_request PRIMARY KEY (id)
);
