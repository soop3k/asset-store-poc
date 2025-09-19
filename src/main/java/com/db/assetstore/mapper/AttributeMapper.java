package com.db.assetstore.mapper;

import com.db.assetstore.jpa.AssetEntity;
import com.db.assetstore.jpa.AttributeEntity;
import com.db.assetstore.model.AttributeValue;

import java.time.Instant;

/**
 * Focused mapper for attribute conversions between JPA entity and domain model.
 * We intentionally keep it as a small hand-written mapper because AttributeValue
 * carries a generic payload and requires string serialization with an explicit value type.
 */
public final class AttributeMapper {

    private AttributeMapper() {}

    public static AttributeEntity toEntity(AssetEntity parent, AttributeValue<?> av, Instant when) {
        String sval = stringify(av.value());
        String vtype = typeName(av.type());
        return new AttributeEntity(parent, av.name(), sval, vtype, when);
    }

    public static AttributeValue<?> toModel(AttributeEntity e) {
        return parse(e.getName(), e.getValue(), e.getValueType());
    }

    public static AttributeValue<?> parse(String name, String v, String type) {
        if (type == null || type.isBlank() || v == null) {
            return new AttributeValue<>(name, v, String.class);
        }
        return switch (type) {
            case "String" -> new AttributeValue<>(name, v, String.class);
            case "Integer" -> new AttributeValue<>(name, Long.valueOf(Integer.valueOf(v).longValue()), Long.class);
            case "Long" -> new AttributeValue<>(name, Long.valueOf(v), Long.class);
            case "Double" -> new AttributeValue<>(name, Double.valueOf(v), Double.class);
            case "Boolean" -> new AttributeValue<>(name, Boolean.valueOf(v), Boolean.class);
            case "Instant" -> new AttributeValue<>(name, Instant.parse(v), Instant.class);
            default -> new AttributeValue<>(name, v, String.class);
        };
    }

    public static String stringify(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Instant i) {
            return i.toString();
        }
        return String.valueOf(o);
    }

    public static String typeName(Class<?> c) {
        if (c == null) {
            return "String";
        }
        if (c == Instant.class) {
            return "Instant";
        }
        if (c == Integer.class || c == int.class) {
            return "Long";
        }
        if (c == Long.class || c == long.class) {
            return "Long";
        }
        if (c == Double.class || c == double.class) {
            return "Double";
        }
        if (c == Boolean.class || c == boolean.class) {
            return "Boolean";
        }
        return "String";
    }
}
