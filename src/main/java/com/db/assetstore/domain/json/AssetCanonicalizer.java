package com.db.assetstore.domain.json;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;


@Component
@RequiredArgsConstructor
public final class AssetCanonicalizer {

    private final ObjectMapper mapper;

    public String toCanonicalJson(Asset asset) {
        ObjectNode root = mapper.createObjectNode();
        put(root, "id", asset.getId());
        put(root, "type", asset.getType());
        put(root, "createdAt", asset.getCreatedAt());
        put(root, "version", asset.getVersion());
        put(root, "status", asset.getStatus());
        put(root, "subtype", asset.getSubtype());
        put(root, "statusEffectiveTime", asset.getStatusEffectiveTime());
        put(root, "modifiedAt", asset.getModifiedAt());
        put(root, "modifiedBy", asset.getModifiedBy());
        put(root, "createdBy", asset.getCreatedBy());
        root.put("softDelete", asset.isSoftDelete());
        put(root, "notionalAmount", asset.getNotionalAmount());
        put(root, "year", asset.getYear());
        put(root, "wh", asset.getWh());
        put(root, "sourceSystemName", asset.getSourceSystemName());
        put(root, "externalReference", asset.getExternalReference());
        put(root, "description", asset.getDescription());
        put(root, "currency", asset.getCurrency());

        ObjectNode attrs = mapper.createObjectNode();
        AttributeNodeWriter writer = new AttributeNodeWriter(attrs);
        asset.getAttributesByName().forEach((name, values) -> writer.write(name, first(values)));
        root.set("attributes", attrs);

        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize canonical asset JSON", e);
        }
    }

    private static AttributeValue<?> first(List<AttributeValue<?>> values) {
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    private static void put(ObjectNode node, String field, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String s) {
            node.put(field, s);
        } else if (value instanceof Integer i) {
            node.put(field, i);
        } else if (value instanceof Long l) {
            node.put(field, l);
        } else if (value instanceof BigDecimal bd) {
            node.put(field, bd);
        } else if (value instanceof Boolean b) {
            node.put(field, b);
        } else if (value instanceof Number n) {
            node.put(field, n.doubleValue());
        } else {
            node.put(field, value.toString());
        }
    }

    private static final class AttributeNodeWriter implements AttributeValueVisitor<Void> {
        private final ObjectNode target;
        private String field;

        private AttributeNodeWriter(ObjectNode target) {
            this.target = target;
        }

        private void write(String field, AttributeValue<?> attribute) {
            this.field = field;
            if (attribute == null) {
                target.putNull(field);
            } else {
                attribute.accept(this);
            }
        }

        @Override public Void visitString(String value, String ignored) {
            if (value == null) {
                target.putNull(field);
            } else {
                target.put(field, value);
            }
            return null;
        }

        @Override public Void visitDecimal(BigDecimal value, String ignored) {
            if (value == null) {
                target.putNull(field);
            } else {
                target.put(field, value);
            }
            return null;
        }

        @Override public Void visitBoolean(Boolean value, String ignored) {
            if (value == null) {
                target.putNull(field);
            } else {
                target.put(field, value);
            }
            return null;
        }
    }
}
