package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AssetEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<AssetEntity, String>, JpaSpecificationExecutor<AssetEntity> {
    @EntityGraph(attributePaths = {"attributes"})
    Optional<AssetEntity> findByIdAndDeleted(String id, int deleted);
    List<AssetEntity> findAllByDeleted(int deleted);
}
