package org.pts.document.storage.repository;

import org.pts.document.storage.model.entity.DocumentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends CrudRepository<DocumentEntity, UUID> {
    DocumentEntity findAllByKey(String key);

    List<DocumentEntity> findAllByKeyIn(Collection<String> keys);

    List<DocumentEntity> findAllByIdIn(Collection<UUID> ids);
}
