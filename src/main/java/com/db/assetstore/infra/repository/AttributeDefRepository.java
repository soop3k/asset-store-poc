package com.db.assetstore.infra.repository;

import com.db.assetstore.AssetType;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeDefRepository extends JpaRepository<AttributeDefEntity, Long> {

    List<AttributeDefEntity> findAllByType(AssetType type);
}
