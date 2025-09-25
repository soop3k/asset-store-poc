package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.LinkDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LinkDefinitionRepository extends JpaRepository<LinkDefinitionEntity, Long> {
    Optional<LinkDefinitionEntity> findByEntityTypeAndEntitySubtypeAndActiveTrue(String entityType, String entitySubtype);
}
