package com.db.assetstore.infra.json;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AttributePayloadParser {

    List<ParsedAttributeValue> parse(JsonNode payload) {
        if(payload == null) {
            return List.of();
        }
        var values = new ArrayList<ParsedAttributeValue>();
        var fieldNames = payload.fieldNames();
        while (fieldNames.hasNext()) {
            var name = fieldNames.next();
            var valueNode = payload.get(name);
            if (valueNode == null || valueNode.isNull()) {
                values.add(new ParsedAttributeValue(name, null));
                continue;
            }
            if (valueNode.isArray()) {
                for (var element : valueNode) {
                    values.add(new ParsedAttributeValue(name, element));
                }
                continue;
            }
            values.add(new ParsedAttributeValue(name, valueNode));
        }
        return values;
    }
}
