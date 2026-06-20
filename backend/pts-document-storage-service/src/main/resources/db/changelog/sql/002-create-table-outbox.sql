set search_path to document_storage_schema;

CREATE TABLE outbox
(
    id         BIGSERIAL PRIMARY KEY,

    type       VARCHAR(100) NOT NULL,

    status     VARCHAR(50)  NOT NULL,

    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_status ON outbox (status);
CREATE INDEX idx_outbox_type ON outbox (type);
CREATE INDEX idx_outbox_status_type ON outbox (status, type);