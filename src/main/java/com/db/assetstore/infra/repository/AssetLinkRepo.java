package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AssetLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssetLinkRepo extends JpaRepository<AssetLinkEntity, Long>, JpaSpecificationExecutor<AssetLinkEntity> {

    @Query("""
        select l from AssetLinkEntity l
        where l.assetId = :assetId and l.active = true
        order by l.id desc
        """)
    List<AssetLinkEntity> activeForAsset(String assetId);

    @Query("""
        select l from AssetLinkEntity l
        where l.assetId = :assetId
        order by l.id desc
        """)
    List<AssetLinkEntity> allForAsset(String assetId);

    default List<AssetLinkEntity> activeForAssetType(String assetId, String entityType, String entitySubtype) {
        return findAll(AssetLinkSpecifications.builder()
                .assetId(assetId)
                .entityType(entityType, entitySubtype)
                .active(true)
                .build());
    }

    @Query("""
        select l from AssetLinkEntity l
        where l.entityType = :entityType
          and l.entitySubtype = :entitySubtype
          and l.targetCode = :targetCode
          and l.active = true
        order by l.id desc
        """)
    List<AssetLinkEntity> activeForTarget(String entityType, String entitySubtype, String targetCode);

    default Optional<AssetLinkEntity> activeLink(String assetId,
                                                 String entityType,
                                                 String entitySubtype,
                                                 String targetCode) {
        return findOne(AssetLinkSpecifications.builder()
                .assetId(assetId)
                .entityType(entityType, entitySubtype)
                .targetCode(targetCode)
                .active(true)
                .build());
    }

    default Optional<AssetLinkEntity> link(String assetId,
                                           String entityType,
                                           String entitySubtype,
                                           String targetCode) {
        return findOne(AssetLinkSpecifications.builder()
                .assetId(assetId)
                .entityType(entityType, entitySubtype)
                .targetCode(targetCode)
                .build());
    }
}
