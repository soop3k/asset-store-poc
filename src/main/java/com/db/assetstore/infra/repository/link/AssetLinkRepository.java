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

    long countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(String assetId, String linkCode, String linkSubtype);

    long countByEntityTypeAndEntityIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(String entityType, String entityId, String linkCode, String linkSubtype);

    boolean existsByAssetIdAndEntityTypeAndEntityIdAndLinkCodeAndLinkSubtypeAndDeletedIsFalse(String assetId, String entityType, String entityId, String linkCode, String linkSubtype);

    Optional<AssetLinkEntity> findByIdAndDeletedIsFalse(String id);
}
