package com.db.assetstore.config;

import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.db.assetstore.repository.JpaAssetRepository;
import com.db.assetstore.repository.AssetRepository;
import com.db.assetstore.service.AssetService;
import com.db.assetstore.service.DefaultAssetService;
import com.db.assetstore.service.EventService;
import com.db.assetstore.service.JsonTransformer;

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

    @Bean
    public JsonTransformer jsonTransformer() {
        return new JsonTransformer();
    }

    @Bean
    public EventService eventService(JsonTransformer transformer) {
        return new EventService(transformer);
    }
}
