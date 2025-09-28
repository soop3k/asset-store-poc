package com.db.assetstore.domain.model;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
@NoArgsConstructor
@AllArgsConstructor
public final class Asset {

    private String id;
    private AssetType type;
    private Instant createdAt;

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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Builder.Default
    private AttributesCollection attributes = AttributesCollection.empty();

    @JsonIgnore
    public List<AttributeValue<?>> getAttributesFlat() {
        return attributes.asList();
    }

    @JsonIgnore
    public Map<String, List<AttributeValue<?>>> getAttributesByName() {
        return attributes.asMap();
    }

    public Asset setAttribute(@NonNull AttributeValue<?> av) {
        this.attributes = this.attributes.add(av);
        return this;
    }

    public Asset setAttributes(@NonNull Collection<AttributeValue<?>> incoming) {
        this.attributes = AttributesCollection.fromFlat(incoming);
        return this;
    }
}
