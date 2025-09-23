package com.db.assetstore.domain.json;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public final class AssetCanonicalizer {
    private final ObjectMapper om;

    public String toCanonicalJson(Asset asset) throws JsonProcessingException {
        ObjectNode root = om.valueToTree(asset);
        root.set("attributes", buildAttributes(asset));
        return om.writeValueAsString(root);
    }

    private JsonNode buildAttributes(Asset asset) {
        ObjectNode attrs = om.createObjectNode();
        asset.getAttributesByName().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> attrs.set(e.getKey(), toNode(e.getValue())));
        return attrs;
    }

    private JsonNode toNode(List<AttributeValue<?>> values) {
        if (values == null || values.isEmpty()) {
            return com.fasterxml.jackson.databind.node.NullNode.getInstance();
        }
        if (values.size() == 1) {
            return om.valueToTree(values.get(0).value());
        }
        ArrayNode arr = om.createArrayNode();
        values.forEach(
                av -> arr.add(om.valueToTree(av == null ? null : av.value()))
        );
        return arr;
    }
}
