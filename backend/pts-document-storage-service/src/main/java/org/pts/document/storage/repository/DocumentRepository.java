package org.pts.document.storage.repository;

import org.pts.document.storage.model.DocumentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentRepository extends CrudRepository<DocumentEntity, UUID> {
}
