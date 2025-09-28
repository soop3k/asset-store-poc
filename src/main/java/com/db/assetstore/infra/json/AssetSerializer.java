package com.db.assetstore.infra.json;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public final class AssetSerializer {

    private final ObjectMapper mapper;

    public String toJson(Asset asset) {
        ObjectNode root = mapper.valueToTree(asset);

        ObjectNode attrs = mapper.createObjectNode();
        AttributeNodeWriter writer = new AttributeNodeWriter(attrs);
        asset.getAttributesFlat().forEach(writer::write);
        root.set("attributes", attrs);

        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize canonical asset JSON", e);
        }
    }

    private static final class AttributeNodeWriter implements AttributeValueVisitor<Void> {
        private final ObjectNode target;
        private String field;

        private AttributeNodeWriter(ObjectNode target) {
            this.target = target;
        }

        private void write(AttributeValue<?> attribute) {
            this.field = attribute.name();
            if (attribute.value() == null) {
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

        @Override
        public Void visitDate(Instant value, String name) {
            if (value == null) {
                target.putNull(field);
            } else {
                target.put(field, value.toString());
            }
            return null;
        }

    }
}
