package com.db.assetstore.infra.json;

import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.time.Instant;

@RequiredArgsConstructor
final class AttributeValueJsonWriter implements AttributeValueVisitor<Void> {

    private final String fieldName;
    private final JsonGenerator gen;
    private final SerializerProvider serializers;

    static AttributeValueJsonWriter forField(String fieldName, JsonGenerator gen, SerializerProvider serializers) {
        return new AttributeValueJsonWriter(fieldName, gen, serializers);
    }

    static AttributeValueJsonWriter forArray(JsonGenerator gen, SerializerProvider serializers) {
        return new AttributeValueJsonWriter(null, gen, serializers);
    }

    @SneakyThrows
    @Override
    public Void visitString(String v, String name) {
        if (fieldName != null) {
            gen.writeStringField(fieldName, v);
        } else {
            gen.writeString(v);
        }
        return null;
    }

    @Override
    public Void visitDecimal(BigDecimal v, String name) {
        writeWithSerializer(v, BigDecimal.class);
        return null;
    }

    @SneakyThrows
    @Override
    public Void visitBoolean(Boolean v, String name) {
        if(fieldName != null) {
            gen.writeBooleanField(fieldName, v);
        } else {
            gen.writeBoolean(v);
        }
        return null;
    }

    @Override
    public Void visitDate(Instant v, String name) {
        writeWithSerializer(v, Instant.class);
        return null;
    }

    @SneakyThrows
    private void writeWithSerializer(Object value, Class<?> type) {
        if(fieldName != null) {
            gen.writeFieldName(fieldName);
        }
        JsonSerializer<Object> serializer = serializers.findValueSerializer(type);
        serializer.serialize(value, gen, serializers);

    }
}