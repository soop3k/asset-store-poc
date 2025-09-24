package com.db.assetstore.infra.repository.link;

import com.db.assetstore.infra.jpa.link.LinkDefinitionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LinkDefinitionRepository extends JpaRepository<LinkDefinitionEntity, String> {

    @EntityGraph(attributePaths = "allowedEntityTypes")
    Optional<LinkDefinitionEntity> findByCode(String code);

    @EntityGraph(attributePaths = "allowedEntityTypes")
    Optional<LinkDefinitionEntity> findByCodeIgnoreCase(String code);
}
