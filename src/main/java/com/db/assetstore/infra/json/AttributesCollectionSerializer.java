package com.db.assetstore.infra.json;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class AttributesCollectionSerializer extends JsonSerializer<AttributesCollection> {

    @Override
    public void serialize(AttributesCollection values, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        if (values != null && !values.isEmpty()) {
            for (Map.Entry<String, List<AttributeValue<?>>> entry : values.asMap().entrySet()) {
                List<AttributeValue<?>> single = entry.getValue();
                if (single == null || single.isEmpty()) {
                    continue;
                }
                if (single.size() == 1) {
                    writeSingle(entry.getKey(), single.get(0), jsonGenerator, serializerProvider);
                } else {
                    writeMany(entry.getKey(), single, jsonGenerator, serializerProvider);
                }
            }
        }
        jsonGenerator.writeEndObject();
    }

    private void writeSingle(String name, AttributeValue<?> attribute, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (noValue(attribute)) {
            gen.writeNullField(name);
            return;
        }
        writeAttribute(attribute, AttributeValueJsonWriter.forField(name, gen, serializers), gen);
    }

    private void writeMany(String name, List<AttributeValue<?>> attributes, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeArrayFieldStart(name);
        var writer = AttributeValueJsonWriter.forArray(gen, serializers);
        for (AttributeValue<?> attribute : attributes) {
            writeAttribute(attribute, writer, gen);
        }
        gen.writeEndArray();
    }

    private void writeAttribute(AttributeValue<?> attribute, AttributeValueJsonWriter writer, JsonGenerator gen) throws IOException {
        if (noValue(attribute)) {
            gen.writeNull();
            return;
        }
        attribute.accept(writer);
    }

    private boolean noValue(AttributeValue<?> attribute) {
        return attribute == null || attribute.value() == null;
    }
}
