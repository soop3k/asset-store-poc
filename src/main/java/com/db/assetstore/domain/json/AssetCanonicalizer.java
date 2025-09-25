package com.db.assetstore.domain.json;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public final class AssetCanonicalizer {

    private final ObjectMapper mapper;

    public String toCanonicalJson(Asset asset) {
        ObjectNode root = mapper.valueToTree(asset);

        ObjectNode attrs = mapper.createObjectNode();
        AttributeValueWriter writer = new AttributeValueWriter(attrs);
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

    private static final class AttributeValueWriter implements AttributeValueVisitor<Void> {
        private final ObjectNode target;
        private String field;

        private AttributeValueWriter(ObjectNode target) {
            this.target = target;
        }

        private void write(String field, AttributeValue<?> attribute) {
            this.field = field;
            putOrNull(attribute, () -> attribute.accept(this));
        }

        @Override public Void visitString(String value, String ignored) {
            putValue(value, v -> target.put(field, v));
            return null;
        }

        @Override public Void visitDecimal(BigDecimal value, String ignored) {
            putValue(value, v -> target.put(field, v));
            return null;
        }

        @Override public Void visitBoolean(Boolean value, String ignored) {
            putValue(value, v -> target.put(field, v));
            return null;
        }

        private void putOrNull(AttributeValue<?> attribute, Runnable writer) {
            if (attribute == null) {
                target.putNull(field);
                return;
            }
            writer.run();
        }

        private <T> void putValue(T value, Consumer<T> writer) {
            if (value == null) {
                target.putNull(field);
                return;
            }
            writer.accept(value);
        }
    }
}
