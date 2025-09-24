package com.db.assetstore.infra.repository.link;

import com.db.assetstore.infra.jpa.link.AssetLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetLinkRepository extends JpaRepository<AssetLinkEntity, String> {

    List<AssetLinkEntity> findByAssetIdAndDeleted(String assetId, boolean deleted);

    List<AssetLinkEntity> findByAssetIdAndActiveAndDeleted(String assetId, boolean active, boolean deleted);

    List<AssetLinkEntity> findByEntityTypeAndEntityIdAndDeleted(String entityType, String entityId, boolean deleted);

    List<AssetLinkEntity> findByEntityTypeAndEntityIdAndActiveAndDeleted(String entityType, String entityId, boolean active, boolean deleted);

    long countByAssetIdAndLinkCodeAndLinkSubtypeAndEntitySubtypeAndActiveIsTrueAndDeletedIsFalse(String assetId, String linkCode, String linkSubtype, String entitySubtype);

    long countByEntityTypeAndEntityIdAndEntitySubtypeAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(String entityType, String entityId, String entitySubtype, String linkCode, String linkSubtype);

    boolean existsByAssetIdAndEntityTypeAndEntityIdAndEntitySubtypeAndLinkCodeAndLinkSubtypeAndDeletedIsFalse(String assetId, String entityType, String entityId, String entitySubtype, String linkCode, String linkSubtype);

    Optional<AssetLinkEntity> findByIdAndDeletedIsFalse(String id);
}
