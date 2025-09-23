package com.db.assetstore.infra.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.db.assetstore.domain.service.EventService;
import com.db.assetstore.domain.service.transform.JsonTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EntityScan(basePackages = "com.db.assetstore.infra.jpa")
public class AssetConfig {

    @Bean
    public JsonTransformer jsonTransformer(ObjectMapper objectMapper) {
        return new JsonTransformer(objectMapper);
    }

}
