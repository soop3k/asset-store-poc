package com.db.assetstore.domain.model;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    /** Abstrakcja dostępu do atrybutów. */
    private AttributesCollection attributes = AttributesCollection.empty();

    @Builder
    public Asset(String id,
                 AssetType type,
                 Instant createdAt,
                 Map<String, List<AttributeValue<?>>> attributes // opcjonalnie: przyjmij mapę w builderze
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.createdAt = Objects.requireNonNullElseGet(createdAt, Instant::now);
        if (attributes != null) this.attributes = AttributesCollection.fromMap(attributes);
    }

    public Map<String, List<AttributeValue<?>>> getAttributesByName() { return attributes.asMap(); }
    public List<AttributeValue<?>> getAttributesFlat() { return attributes.asList(); }

    public void setAttribute(AttributeValue<?> av) { attributes.add(av); }
    public void setAttributes(Collection<AttributeValue<?>> flat) { this.attributes = AttributesCollection.fromFlat(flat); }

}
