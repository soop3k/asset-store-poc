package com.db.assetstore.domain.model.attribute;

import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDate;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AVString.class,  name = "STRING"),
        @JsonSubTypes.Type(value = AVDecimal.class, name = "DECIMAL"),
        @JsonSubTypes.Type(value = AVBoolean.class, name = "BOOLEAN"),
        @JsonSubTypes.Type(value = AVDate.class, name = "DATE")
})
public interface AttributeValue<T> {

    String name();
    T value();
    AttributeValue<T> withValue(T v);
    AttributeType attributeType();

    // Visitor
    <R> R accept(AttributeValueVisitor<R> v);
}
