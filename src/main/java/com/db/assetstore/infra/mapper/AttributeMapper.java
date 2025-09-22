package com.db.assetstore.infra.mapper;

import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MapStruct-backed mapper for attribute conversions between JPA entity and domain model.
 * Only three attribute types are supported system-wide: String, Decimal (BigDecimal), and Boolean.
 */
@Mapper(componentModel = "spring")
public interface AttributeMapper {

    default AttributeEntity toEntity(AssetEntity parent, AttributeValue<?> av) {
        Instant when = Instant.now();
        return av.accept(new MapperAVVisitor(parent, when));
    }

    default AttributeValue<?> toModel(AttributeEntity e) {
        return switch (e.getValueType()) {
            case BOOLEAN -> new AVBoolean(e.getName(), e.getValueBool());
            case DECIMAL -> new AVDecimal(e.getName(), e.getValueNum());
            default -> new AVString(e.getName(), e.getValueStr());
        };
    }

    record MapperAVVisitor(AssetEntity parent, Instant when)
            implements AttributeValueVisitor<AttributeEntity> {

        @Override
        public AttributeEntity visitString(String v, String name) { return new AttributeEntity(parent, name, v, when); }
        @Override
        public AttributeEntity visitDecimal(BigDecimal v, String name) { return new AttributeEntity(parent, name, v, when);}        
        @Override
        public AttributeEntity visitBoolean(Boolean v, String name) { return new AttributeEntity(parent, name, v, when);}    }
}


