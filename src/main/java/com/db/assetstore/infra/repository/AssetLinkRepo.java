package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AssetLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetLinkRepo extends JpaRepository<AssetLinkEntity, Long> {

    List<AssetLinkEntity> findByAssetIdAndActiveTrue(String assetId);

    List<AssetLinkEntity> findByAssetId(String assetId);

    List<AssetLinkEntity> findByAssetIdAndEntityTypeAndEntitySubtypeAndActiveTrue(String assetId,
                                                                                String entityType,
                                                                                String entitySubtype);

    List<AssetLinkEntity> findByEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(String entityType,
                                                                                    String entitySubtype,
                                                                                    String targetCode);

    Optional<AssetLinkEntity> findByAssetIdAndEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(String assetId,
                                                                                                  String entityType,
                                                                                                  String entitySubtype,
                                                                                                  String targetCode);

    default List<AssetLinkEntity> activeAsset(String assetId) {
        return findByAssetIdAndActiveTrue(assetId);
    }

    default List<AssetLinkEntity> allAsset(String assetId) {
        return findByAssetId(assetId);
    }

    default List<AssetLinkEntity> activeAssetType(String assetId, String entityType, String entitySubtype) {
        return findByAssetIdAndEntityTypeAndEntitySubtypeAndActiveTrue(assetId, entityType, entitySubtype);
    }

    default List<AssetLinkEntity> activeTarget(String entityType, String entitySubtype, String targetCode) {
        return findByEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(entityType, entitySubtype, targetCode);
    }

    default Optional<AssetLinkEntity> activeMatch(String assetId,
                                                  String entityType,
                                                  String entitySubtype,
                                                  String targetCode) {
        return findByAssetIdAndEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(assetId, entityType, entitySubtype, targetCode);
    }
}
