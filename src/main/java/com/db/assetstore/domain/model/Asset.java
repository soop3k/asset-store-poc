package com.db.assetstore.domain.model;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public final class Asset {

    private final String id;
    private final AssetType type;
    private final Instant createdAt;

    @Setter private Long version;
    @Setter private String status;
    @Setter private String subtype;
    @Setter private Instant statusEffectiveTime;
    @Setter private Instant modifiedAt;
    @Setter private String modifiedBy;
    @Setter private String createdBy;
    @Setter private boolean softDelete;
    @Setter private BigDecimal notionalAmount;
    @Setter private Integer year;
    @Setter private String wh;
    @Setter private String sourceSystemName;
    @Setter private String externalReference;
    @Setter private String description;
    @Setter private String currency;

    @JsonIgnore
    private AttributesCollection attributes;

    @JsonIgnore
    public List<AttributeValue<?>> getAttributesFlat() {
        return attributes.asListView();
    }

    @JsonProperty("attributes")
    public Map<String, AttributeValue<?>> getAttributesJson() {
        Map<String, AttributeValue<?>> out = new LinkedHashMap<>();
        if (attributes == null || attributes.isEmpty()) {
            return out;
        }
        var grouped = attributes.asMapView();
        if (grouped.isEmpty()) {
            return out;
        }
        grouped.forEach((name, list) -> {
            if (list != null && !list.isEmpty()) {
                out.put(name, list.get(0));
            }
        });
        return out;
    }

    @JsonIgnore
    public Map<String, List<AttributeValue<?>>> getAttributesByName() {
        return attributes.asMapView();
    }

    public <T> Optional<T> getAttr(String name, Class<T> type) {
        return attributes.getOne(name, type);
    }

    public <T> List<T> getAttrs(String name, Class<T> type) {
        return attributes.getMany(name, type);
    }

    /**
     * Append a single attribute value to the collection (non-destructive for other attributes).
     */
    public Asset setAttribute(AttributeValue<?> av) {
        if (av == null) {
            return this;
        }
        this.attributes = this.attributes.add(av);
        return this;
    }

    /**
     * Replace all attributes with the provided flat collection.
     */
    public Asset setAttributes(Collection<AttributeValue<?>> incoming) {
        var safeIncoming = incoming == null ? List.<AttributeValue<?>>of() : incoming;
        this.attributes = AttributesCollection.fromFlat(safeIncoming);
        return this;
    }
}
