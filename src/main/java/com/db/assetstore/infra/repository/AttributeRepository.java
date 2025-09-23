package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {
    // Intentionally no finder methods for attributes; access via AssetEntity.getAttributes() only
}
