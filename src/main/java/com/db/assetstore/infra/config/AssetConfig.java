package com.db.assetstore.infra.config;

import com.db.assetstore.domain.service.AssetService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.db.assetstore.domain.service.EventService;
import com.db.assetstore.domain.service.transform.JsonTransformer;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeHistoryRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.AttributeDefRepository;

@Configuration
@EntityScan(basePackages = "com.db.assetstore.infra.jpa")
public class AssetConfig {

    @Bean
    public JsonTransformer jsonTransformer() {
        return new JsonTransformer();
    }

    @Bean
    public EventService eventService(JsonTransformer transformer) {
        return new EventService(transformer);
    }
}
