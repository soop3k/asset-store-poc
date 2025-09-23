package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AttributeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeHistoryRepository extends JpaRepository<AttributeHistoryEntity, Long> {
    List<AttributeHistoryEntity> findAllByAsset_IdOrderByChangedAt(String assetId);
}
