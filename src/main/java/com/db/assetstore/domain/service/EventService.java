package com.db.assetstore.domain.service;

import com.db.assetstore.infra.json.AssetSerializer;
import com.db.assetstore.domain.model.asset.Asset;
import com.db.assetstore.domain.service.transform.JsonTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public final class EventService {
    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final JsonTransformer transformer;
    private final AssetSerializer canonicalizer;
    private final ObjectMapper objectMapper;

    public String generate(@NonNull String eventName, @NonNull Asset asset) throws JsonProcessingException {
        log.debug("Generating event '{}' for asset id={} type={}", eventName, asset.getId(), asset.getType());
        String canonical = canonicalizer.toJson(asset);

        ObjectNode ctx = objectMapper.createObjectNode();
        ctx.put("eventName", eventName);
        ctx.put("occurredAt", Instant.now().toString());
        try {
            ctx.set("asset", objectMapper.readTree(canonical));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse canonical asset JSON: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to parse canonical asset JSON", e);
        }
        String inputForTransform;
        try {
            inputForTransform = objectMapper.writeValueAsString(ctx);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize transform input: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to serialize transform input", e);
        }

        String transformed = transformer.transform(eventName, inputForTransform);
        log.debug("Generated event '{}' successfully", eventName);
        return transformed;
    }

}
