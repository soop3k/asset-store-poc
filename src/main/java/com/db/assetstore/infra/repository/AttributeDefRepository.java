package com.db.assetstore.infra.repository;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttributeDefRepository extends JpaRepository<AttributeDefEntity, Long> {

    @Query("select distinct ad from AttributeDefEntity ad " +
            "left join fetch ad.constraints where ad.type = :type")
    List<AttributeDefEntity> findAllByTypeWithConstraints(@Param("type") AssetType type);
}
