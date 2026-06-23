set search_path to document_storage_schema;

CREATE TABLE IF NOT EXISTS document
(
    id                 UUID PRIMARY KEY,

    document_key       VARCHAR(255),

    temp_key           VARCHAR(255) NOT NULL,
    temp_bucket        VARCHAR(255) NOT NULL,

    encrypted_file_key BYTEA,
    iv                 BYTEA,

    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),

    status             VARCHAR(50)  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_document_status ON document (status);