package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AssetLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetLinkRepository extends JpaRepository<AssetLinkEntity, Long> {

    List<AssetLinkEntity> findByAssetIdAndActive(String assetId, boolean active);

    List<AssetLinkEntity> findByAssetId(String assetId);

    List<AssetLinkEntity> findByAssetIdAndEntityTypeAndEntitySubtypeAndActiveTrue(String assetId, String entityType, String entitySubtype);

    List<AssetLinkEntity> findByEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(String entityType, String entitySubtype, String targetCode);

    Optional<AssetLinkEntity> findByAssetIdAndEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(String assetId, String entityType, String entitySubtype, String targetCode);
}
