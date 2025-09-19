package com.db.assetstore.model;

import com.db.assetstore.AssetType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
public final class Asset {
    private String id; // UUID as string for DB portability
    private AssetType type;
    private Instant createdAt;
    // Additional asset-level properties (optional, may be null if not provided)
    private Long version = null;
    private String status = null;
    private String subtype = null;
    private Instant statusEffectiveTime = null;
    private Instant modifiedAt = null;
    private String modifiedBy = null;
    private String createdBy = null;
    private boolean softDelete = false;
    private BigDecimal notionalAmount = null;
    private Integer year = null;
    private String wh = null;
    private String sourceSystemName = null;
    private String externalReference = null;
    private String description = null;
    private String currency = null;
    // Keep a mutable map internally to allow in-place updates on a loaded Asset
    private Map<String, AttributeValue<?>> attributes;

    @Builder
    public Asset(String id, AssetType type, Instant createdAt, Collection<AttributeValue<?>> attrs) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.createdAt = Objects.requireNonNullElseGet(createdAt, Instant::now);
        Map<String, AttributeValue<?>> map = new LinkedHashMap<>();
        if (attrs != null) {
            for (AttributeValue<?> a : attrs) map.put(a.name(), a);
        }
        this.attributes = map; // keep mutable for setAttribute(s)
    }

    // Expose an unmodifiable view to callers to prevent accidental external mutation
    public Map<String, AttributeValue<?>> attributes() { return Collections.unmodifiableMap(attributes); }

    public static Asset ofNew(AssetType type, AttributeValue<?>... attrs) {
        return new Asset(UUID.randomUUID().toString(), type, Instant.now(), Arrays.asList(attrs));
    }

    // New: in-place update of a single attribute on this Asset instance
    public void setAttribute(AttributeValue<?> attr) {
        if (attr == null) {
            return;
        }
        this.attributes.put(attr.name(), attr);
    }

    // New: in-place update by name using generics
    public <T> void setAttribute(String name, T value, Class<T> type) {
        if (name == null || type == null) {
            return;
        }
        setAttribute(new AttributeValue<>(name, value, type));
    }

    // New: in-place update of multiple attributes on this Asset instance
    public void setAttributes(Collection<AttributeValue<?>> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return;
        }
        for (AttributeValue<?> a : attrs) {
            if (a != null) {
                this.attributes.put(a.name(), a);
            }
        }
    }

    // New: get a single attribute value using generics in a type-safe way
    public <T> Optional<T> getAttribute(String name, Class<T> type) {
        if (name == null || type == null) {
            return Optional.empty();
        }
        AttributeValue<?> av = this.attributes.get(name);
        if (av == null) {
            return Optional.empty();
        }
        // Ensure requested type matches the stored attribute type; if not, signal an error
        if (!type.isAssignableFrom(av.type())) {
            throw new ClassCastException("Attribute '" + name + "' has type " + av.type().getName() + " but was requested as " + type.getName());
        }
        // Safe cast using the provided Class to avoid unchecked warnings
        T cast = type.cast(av.value());
        return Optional.ofNullable(cast);
    }
    
    // One-arg overload: generic return; delegates to the typed variant for checks using stored type
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttribute(String name) {
        if (name == null) {
            return Optional.empty();
        }
        AttributeValue<?> av = this.attributes.get(name);
        if (av == null) {
            return Optional.empty();
        }
        Class<?> t = av.type();
        try {
            return (Optional<T>)  getAttribute(name, t);
        } catch (ClassCastException e) {
            // Re-throw with the same message style as the typed variant
            throw new ClassCastException("Attribute '" + name + "' has type " + t.getName() + " but was requested as " + t.getName());
        }
    }

}
