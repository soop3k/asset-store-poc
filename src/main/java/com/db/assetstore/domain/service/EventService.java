package com.db.assetstore.domain.service;

import com.db.assetstore.domain.json.AssetCanonicalizer;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.service.transform.JsonTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class EventService {
    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private static final ObjectMapper M = new ObjectMapper();

    private final JsonTransformer transformer;
    private final AssetCanonicalizer canonicalizer;

    public EventService(JsonTransformer transformer) {
        this.transformer = Objects.requireNonNull(transformer);
        this.canonicalizer = new AssetCanonicalizer();
    }

    public String generate(String eventName, Asset asset) {
        Objects.requireNonNull(eventName, "eventName");
        Objects.requireNonNull(asset, "asset");
        log.debug("Generating event '{}' for asset id={} type={}", eventName, asset.getId(), asset.getType());
        String canonical = canonicalizer.toCanonicalJson(asset);

        // Build envelope for transformation
        ObjectNode ctx = M.createObjectNode();
        ctx.put("eventName", eventName);
        ctx.put("occurredAt", Instant.now().toString());
        try {
            ctx.set("asset", M.readTree(canonical));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse canonical asset JSON: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to parse canonical asset JSON", e);
        }
        String inputForTransform;
        try {
            inputForTransform = M.writeValueAsString(ctx);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize transform input: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to serialize transform input", e);
        }

        // transform using JSLT
        String transformed = transformer.transform("events/" + eventName, inputForTransform);
        // Schema validation intentionally disabled; unknown extra fields in downstream models are ignored at consumers.
        log.debug("Generated event '{}' successfully", eventName);
        return transformed;
    }

}
