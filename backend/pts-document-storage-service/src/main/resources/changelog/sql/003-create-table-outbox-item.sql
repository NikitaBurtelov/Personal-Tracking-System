set search_path to document_storage_schema;

CREATE TABLE outbox_item
(
    id          BIGINT PRIMARY KEY,

    job_id      BIGINT      NOT NULL,

    document_id UUID        NOT NULL,

    status      VARCHAR(50) NOT NULL
);

CREATE INDEX idx_outbox_item_job_id ON outbox_item (job_id);
CREATE INDEX idx_outbox_item_status ON outbox_item (status);
CREATE INDEX idx_outbox_item_job_status
    ON outbox_item (job_id, status);