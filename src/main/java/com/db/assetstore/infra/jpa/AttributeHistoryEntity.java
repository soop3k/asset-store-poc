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

    @Column(name = "value_date")
    private Instant valueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", length = 32, nullable = false)
    private AttributeType valueType;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public AttributeHistoryEntity(@NonNull AttributeEntity attribute, @NonNull Instant when) {
        this.attribute = attribute;
        asset     = attribute.getAsset();
        name      = attribute.getName();
        valueType = attribute.getValueType();
        valueStr  = attribute.getValueStr();
        valueNum  = attribute.getValueNum();
        valueBool = attribute.getValueBool();
        valueDate = attribute.getValueDate();
        changedAt = when;
    }
}
