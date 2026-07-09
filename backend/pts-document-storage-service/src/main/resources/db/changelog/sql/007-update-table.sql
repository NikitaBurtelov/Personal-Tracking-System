-- changeset nikita:1783438375281-3
ALTER TABLE document_storage_schema.outbox
    ADD operation_id UUID;

-- changeset nikita:1783438375281-4
ALTER TABLE document_storage_schema.outbox
    ALTER COLUMN operation_id SET NOT NULL;

-- changeset nikita:1783438375281-1
ALTER TABLE document_storage_schema.outbox ALTER COLUMN status TYPE VARCHAR(255) USING (status::VARCHAR(255));

-- changeset nikita:1783438375281-2
ALTER TABLE document_storage_schema.outbox ALTER COLUMN type TYPE VARCHAR(255) USING (type::VARCHAR(255));

