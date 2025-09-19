package com.db.assetstore.config;

import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.db.assetstore.repo.JpaAssetRepository;
import com.db.assetstore.repo.AssetRepository;
import com.db.assetstore.service.AssetService;
import com.db.assetstore.service.DefaultAssetService;

@Configuration
@EntityScan(basePackages = "com.db.assetstore.jpa")
public class AssetConfig {

    @Bean
    public AssetRepository assetRepository(EntityManager entityManager) {
        return new JpaAssetRepository(entityManager);
    }

    @Bean
    public AssetService assetService(AssetRepository repository) {
        return new DefaultAssetService(repository);
    }
}
