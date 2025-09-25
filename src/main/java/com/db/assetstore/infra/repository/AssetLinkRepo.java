package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AssetLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetLinkRepo extends JpaRepository<AssetLinkEntity, Long> {

    List<AssetLinkEntity> findAllByAssetIdAndActiveTrue(String assetId);

    List<AssetLinkEntity> findAllByAssetId(String assetId);

    List<AssetLinkEntity> findAllByAssetIdAndEntityTypeAndEntitySubtypeAndActiveTrue(String assetId,
                                                                                    String entityType,
                                                                                    String entitySubtype);

    List<AssetLinkEntity> findAllByEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(String entityType,
                                                                                        String entitySubtype,
                                                                                        String targetCode);

    Optional<AssetLinkEntity> findFirstByAssetIdAndEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(String assetId,
                                                                                                       String entityType,
                                                                                                       String entitySubtype,
                                                                                                       String targetCode);

    Optional<AssetLinkEntity> findFirstByAssetIdAndEntityTypeAndEntitySubtypeAndTargetCode(String assetId,
                                                                                           String entityType,
                                                                                           String entitySubtype,
                                                                                           String targetCode);

    default List<AssetLinkEntity> activeForAsset(String assetId) {
        return findAllByAssetIdAndActiveTrue(assetId);
    }

    default List<AssetLinkEntity> allForAsset(String assetId) {
        return findAllByAssetId(assetId);
    }

    default List<AssetLinkEntity> activeForAssetType(String assetId, String entityType, String entitySubtype) {
        return findAllByAssetIdAndEntityTypeAndEntitySubtypeAndActiveTrue(assetId, entityType, entitySubtype);
    }

    default List<AssetLinkEntity> activeForTarget(String entityType, String entitySubtype, String targetCode) {
        return findAllByEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(entityType, entitySubtype, targetCode);
    }

    default Optional<AssetLinkEntity> activeLinkForAsset(String assetId,
                                                         String entityType,
                                                         String entitySubtype,
                                                         String targetCode) {
        return findFirstByAssetIdAndEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(assetId, entityType, entitySubtype, targetCode);
    }

    default Optional<AssetLinkEntity> linkForAsset(String assetId,
                                                   String entityType,
                                                   String entitySubtype,
                                                   String targetCode) {
        return findFirstByAssetIdAndEntityTypeAndEntitySubtypeAndTargetCode(assetId, entityType, entitySubtype, targetCode);
    }
}
