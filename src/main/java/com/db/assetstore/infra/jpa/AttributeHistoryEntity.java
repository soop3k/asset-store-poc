package com.db.assetstore.infra.jpa;

import com.db.assetstore.domain.model.AttributeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "asset_attribute_history")
@Getter @Setter @NoArgsConstructor
public class AttributeHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // właściciel historii
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

    public static AttributeHistoryEntity fromCurrent(AttributeEntity attribute, Instant when) {
        AttributeHistoryEntity h = new AttributeHistoryEntity();
        h.attribute = Objects.requireNonNull(attribute);
        h.asset     = attribute.getAsset();
        h.name      = attribute.getName();
        h.valueType = attribute.getValueType();
        h.valueStr  = attribute.getValueStr();
        h.valueNum  = attribute.getValueNum();
        h.valueBool = attribute.getValueBool();
        h.changedAt = Objects.requireNonNull(when);
        return h;
    }
}
