package com.db.assetstore.domain.model;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
@JsonIgnoreProperties(value = {"attributesByName", "attributesFlat"}, allowGetters = true)
public final class Asset {

    private String id;
    private AssetType type;
    private Instant createdAt;

    // meta...
    private Long version;
    private String status;
    private String subtype;
    private Instant statusEffectiveTime;
    private Instant modifiedAt;
    private String modifiedBy;
    private String createdBy;
    private boolean softDelete;
    private BigDecimal notionalAmount;
    private Integer year;
    private String wh;
    private String sourceSystemName;
    private String externalReference;
    private String description;
    private String currency;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    private AttributesCollection attributes = AttributesCollection.empty();

    @Builder
    public Asset(String id,
                 AssetType type,
                 Instant createdAt,
                 AttributesCollection attributes
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.createdAt = Objects.requireNonNullElseGet(createdAt, Instant::now);
        if (attributes != null) {
            this.attributes = AttributesCollection.fromMap(attributes.asMap());
        }
    }

    public Map<String, List<AttributeValue<?>>> getAttributesByName() { return attributes.asMap(); }
    public List<AttributeValue<?>> getAttributesFlat() { return attributes.asList(); }

    @JsonProperty("attributes")
    public Map<String, Object> jsonAttributes() {
        Map<String, List<AttributeValue<?>>> map = attributes.asMap();
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        for (var e : map.entrySet()) {
            List<AttributeValue<?>> vs = e.getValue();
            if (vs == null || vs.isEmpty()) continue;
            // For API simplicity, expose only the first value for each attribute name
            out.put(e.getKey(), vs.get(0));
        }
        return out;
    }

    public void setAttribute(AttributeValue<?> av) { attributes.add(av); }
    public void setAttributes(List<AttributeValue<?>> flat) { this.attributes = AttributesCollection.fromFlat(flat); }

}
