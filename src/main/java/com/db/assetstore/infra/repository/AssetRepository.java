package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<AssetEntity, String>, JpaSpecificationExecutor<AssetEntity> {

    @Query("select a from AssetEntity a left join fetch a.attributes where a.id = :id and a.deleted = :deleted")
    Optional<AssetEntity> findByIdAndDeleted(@Param("id") String id, @Param("deleted") int deleted);
    List<AssetEntity> findAllByDeleted(int deleted);
}
