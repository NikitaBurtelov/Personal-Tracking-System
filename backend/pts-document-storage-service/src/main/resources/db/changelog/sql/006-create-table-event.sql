set search_path to document_storage_schema;

CREATE TABLE IF NOT EXISTS outbox_event
(
    id         UUID    NOT NULL,
    operation_id UUID    NOT NULL,
    payload    JSONB,
    status     SMALLINT,
    published  BOOLEAN NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_outbox_event PRIMARY KEY (id)
);
