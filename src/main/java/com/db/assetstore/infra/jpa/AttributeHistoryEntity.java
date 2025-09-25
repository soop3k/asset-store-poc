package com.db.assetstore.infra.jpa;

import com.db.assetstore.domain.model.type.AttributeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Table(name = "asset_attribute_history")
@Getter @Setter @NoArgsConstructor
public class AttributeHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeEntity attribute;

    // redundancja dla wygodnych zapytań
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private AssetEntity asset;

    @Column(length = 128, nullable = false)
    private String name;

    @Column(name = "value_str", length = 1024)
    private String valueStr;

    @Column(name = "value_bool")
    private Boolean valueBool;

    @Column(name = "value_num", precision = 38, scale = 10)
    private BigDecimal valueNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", length = 32, nullable = false)
    private AttributeType valueType;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public AttributeHistoryEntity(@NonNull AttributeEntity attribute, @NonNull Instant when) {
        this.attribute = attribute;
        this.asset     = attribute.getAsset();
        this.name      = attribute.getName();
        this.valueType = attribute.getValueType();
        this.valueStr  = attribute.getValueStr();
        this.valueNum  = attribute.getValueNum();
        this.valueBool = attribute.getValueBool();
        this.changedAt = when;
    }
}
