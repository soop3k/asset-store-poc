package com.db.assetstore.domain.json;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * Builds canonical JSON representation for Asset entities used as input for transformations/events.
 * Extracted to reduce complexity of EventService and promote reuse.
 */
public final class AssetCanonicalizer {
    private static final ObjectMapper M = new ObjectMapper();

    public String toCanonicalJson(Asset asset) {
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
        for (Map.Entry<String, AttributeValue<?>> e : asset.getAttributesByName().entrySet()) {
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
