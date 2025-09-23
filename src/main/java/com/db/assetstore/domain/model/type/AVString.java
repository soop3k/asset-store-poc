package com.db.assetstore.domain.model.type;


import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;

public record AVString(String name, String value)
        implements AttributeValue<String> {

    @Override public AttributeValue<String> withValue(String v) {
        return new AVString(name, v);
    }

    @Override public AttributeType attributeType() {
        return AttributeType.STRING;
    }

    @Override public <R> R accept(AttributeValueVisitor<R> v) {
        return v.visitString(value, name);
    }

    public static AVString of(String name, String s) {
        return new AVString(name, s);
    }
}

