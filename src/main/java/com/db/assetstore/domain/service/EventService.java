package com.db.assetstore.domain.service;

import com.db.assetstore.domain.exception.CanonicalizationException;
import com.db.assetstore.domain.exception.EventGenerationException;
import com.db.assetstore.domain.exception.JsonTransformException;
import com.db.assetstore.domain.json.AssetCanonicalizer;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.service.transform.JsonTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

/**
 * Service that generates event JSON from an Asset using JSLT templates.
 *
 * How it works:
 * 1) Convert Asset to canonical JSON.
 * 2) Apply transforms/events/{eventName}.jslt to produce the event JSON.
 *
 * Only JSLT is used for transformation; schema validations are intentionally not applied.
 * By adding transforms/events/{eventName}.jslt, new events can be added without changing Java code.
 */
@Component
@RequiredArgsConstructor
public final class EventService {
    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final JsonTransformer transformer;
    private final AssetCanonicalizer canonicalizer;
    private final ObjectMapper objectMapper;

    public String generate(String eventName, Asset asset)
            throws CanonicalizationException, JsonTransformException, EventGenerationException {
        Objects.requireNonNull(eventName, "eventName");
        Objects.requireNonNull(asset, "asset");
        log.debug("Generating event '{}' for asset id={} type={}", eventName, asset.getId(), asset.getType());
        String canonical = canonicalizer.toCanonicalJson(asset);

        ObjectNode ctx = objectMapper.createObjectNode();
        ctx.put("eventName", eventName);
        ctx.put("occurredAt", Instant.now().toString());
        try {
            ctx.set("asset", objectMapper.readTree(canonical));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse canonical asset JSON for asset {}: {}", asset.getId(), e.getMessage(), e);
            throw new EventGenerationException(
                    "Failed to parse canonical asset JSON for asset " + asset.getId(), e);
        }
        String inputForTransform;
        try {
            inputForTransform = objectMapper.writeValueAsString(ctx);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize transform input for event {} and asset {}: {}",
                    eventName, asset.getId(), e.getMessage(), e);
            throw new EventGenerationException(
                    "Failed to serialize transform input for event %s and asset %s".formatted(eventName, asset.getId()), e);
        }

        String transformed = transformer.transform(eventName, inputForTransform);
        log.debug("Generated event '{}' successfully", eventName);
        return transformed;
    }

}
