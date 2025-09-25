package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AssetLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetLinkRepo extends JpaRepository<AssetLinkEntity, Long> {

    @Query("select link from AssetLinkEntity link where link.assetId = :assetId and link.active = true")
    List<AssetLinkEntity> findActiveByAssetId(@Param("assetId") String assetId);

    List<AssetLinkEntity> findByAssetId(String assetId);

    @Query("select link from AssetLinkEntity link where link.assetId = :assetId and link.entityType = :entityType and link.entitySubtype = :entitySubtype and link.active = true")
    List<AssetLinkEntity> findActiveByAsset(@Param("assetId") String assetId,
                                           @Param("entityType") String entityType,
                                           @Param("entitySubtype") String entitySubtype);

    @Query("select link from AssetLinkEntity link where link.entityType = :entityType and link.entitySubtype = :entitySubtype and link.targetCode = :targetCode and link.active = true")
    List<AssetLinkEntity> findActiveByTarget(@Param("entityType") String entityType,
                                            @Param("entitySubtype") String entitySubtype,
                                            @Param("targetCode") String targetCode);

    @Query("select link from AssetLinkEntity link where link.assetId = :assetId and link.entityType = :entityType and link.entitySubtype = :entitySubtype and link.targetCode = :targetCode and link.active = true")
    Optional<AssetLinkEntity> findActive(@Param("assetId") String assetId,
                                         @Param("entityType") String entityType,
                                         @Param("entitySubtype") String entitySubtype,
                                         @Param("targetCode") String targetCode);
}
