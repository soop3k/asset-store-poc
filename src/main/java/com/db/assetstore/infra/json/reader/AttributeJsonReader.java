package com.db.assetstore.infra.json.reader;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class AttributeJsonReader {

    private final AttributePayloadParser payloadParser;
    private final AttributeValueAssembler valueAssembler;

    public AttributeJsonReader(AttributePayloadParser payloadParser,
                               AttributeValueAssembler valueAssembler) {
        this.payloadParser = payloadParser;
        this.valueAssembler = valueAssembler;
    }

    public AttributesCollection read(AssetType type, JsonNode payload) {
        var rawValues = payloadParser.parse(payload);
        return valueAssembler.assemble(type, rawValues);
    }
}
