package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AssetHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetHistoryRepository extends JpaRepository<AssetHistoryEntity, Long> {
    List<AssetHistoryEntity> findAllByAsset_IdOrderByChangedAt(String assetId);
}
