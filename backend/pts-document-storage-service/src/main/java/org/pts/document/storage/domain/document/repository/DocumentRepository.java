package org.pts.document.storage.domain.document.repository;

import org.pts.document.storage.domain.document.entity.DocumentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends CrudRepository<DocumentEntity, UUID> {
    DocumentEntity findAllByObjectKey(String key);

    List<DocumentEntity> findAllByObjectKeyIn(Collection<String> keys);

    List<DocumentEntity> findAllByIdIn(Collection<UUID> ids);
}
