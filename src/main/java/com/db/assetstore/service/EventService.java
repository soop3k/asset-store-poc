package com.db.assetstore.service;

import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Objects;

/**
 * Service that generates event JSON from an Asset using JSLT templates.
 *
 * How it works:
 * 1) Convert Asset to canonical JSON (validated against schemas/asset.json.schema).
 * 2) Apply transforms/events/{eventName}.jslt to produce the event JSON.
 * 3) Validate the result against schemas/events/{eventName}.schema.json (if present).
 *
 * By adding transforms/events/{eventName}.jslt (and optional schema), new events can be added
 * without changing Java code.
 */
public final class EventService {
    private static final ObjectMapper M = new ObjectMapper();

    private final JsonTransformer transformer;

    public EventService() {
        this.transformer = new JsonTransformer();
    }

    public EventService(JsonTransformer transformer) {
        this.transformer = Objects.requireNonNull(transformer);
    }

    public String generate(String eventName, Asset asset) {
        Objects.requireNonNull(eventName, "eventName");
        Objects.requireNonNull(asset, "asset");
        String canonical = toCanonicalJson(asset);
        // validate canonical input
        JsonSchemaValidator.validateOrThrow(canonical, "schemas/asset.json.schema");

        // Build envelope for transformation
        com.fasterxml.jackson.databind.node.ObjectNode ctx = M.createObjectNode();
        ctx.put("eventName", eventName);
        ctx.put("occurredAt", java.time.Instant.now().toString());
        try {
            ctx.set("asset", (com.fasterxml.jackson.databind.JsonNode) M.readTree(canonical));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse canonical asset JSON", e);
        }
        String inputForTransform;
        try {
            inputForTransform = M.writeValueAsString(ctx);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize transform input", e);
        }

        // transform using JSLT
        String transformed = transformer.transform("events/" + eventName, inputForTransform);
        // validate output if schema exists
        JsonSchemaValidator.validateIfPresent(transformed, "schemas/events/" + eventName + ".schema.json");
        return transformed;
    }

    // Canonical JSON for Asset that matches schemas/asset.json.schema
    private static String toCanonicalJson(Asset asset) {
        ObjectNode root = M.createObjectNode();
        if (asset.getId() != null) root.put("id", asset.getId());
        if (asset.getType() != null) root.put("type", asset.getType().name());
        if (asset.getCreatedAt() != null) root.put("createdAt", asset.getCreatedAt().toString());
        if (asset.getVersion() != null) root.put("version", asset.getVersion());
        if (asset.getStatus() != null) root.put("status", asset.getStatus());
        if (asset.getSubtype() != null) root.put("subtype", asset.getSubtype());
        if (asset.getStatusEffectiveTime() != null) root.put("statusEffectiveTime", asset.getStatusEffectiveTime().toString());
        if (asset.getModifiedAt() != null) root.put("modifiedAt", asset.getModifiedAt().toString());
        if (asset.getModifiedBy() != null) root.put("modifiedBy", asset.getModifiedBy());
        if (asset.getCreatedBy() != null) root.put("createdBy", asset.getCreatedBy());
        root.put("softDelete", asset.isSoftDelete());
        if (asset.getNotionalAmount() != null) root.put("notionalAmount", asset.getNotionalAmount());
        if (asset.getYear() != null) root.put("year", asset.getYear());
        if (asset.getWh() != null) root.put("wh", asset.getWh());
        if (asset.getSourceSystemName() != null) root.put("sourceSystemName", asset.getSourceSystemName());
        if (asset.getExternalReference() != null) root.put("externalReference", asset.getExternalReference());
        if (asset.getDescription() != null) root.put("description", asset.getDescription());
        if (asset.getCurrency() != null) root.put("currency", asset.getCurrency());

        ObjectNode attrs = M.createObjectNode();
        for (Map.Entry<String, AttributeValue<?>> e : asset.attributes().entrySet()) {
            String name = e.getKey();
            Object val = e.getValue().value();
            if (val == null) {
                attrs.putNull(name);
            } else if (val instanceof Number n) {
                if (n instanceof Integer i) attrs.put(name, i.intValue());
                else if (n instanceof Long l) attrs.put(name, l);
                else if (n instanceof Double d) attrs.put(name, d);
                else attrs.put(name, n.doubleValue());
            } else if (val instanceof Boolean b) {
                attrs.put(name, b);
            } else {
                attrs.put(name, String.valueOf(val));
            }
        }
        root.set("attributes", attrs);

        try {
            return M.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize canonical asset JSON", e);
        }
    }
}
