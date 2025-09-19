package com.db.assetstore.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "asset_attribute_history")
@Getter
@Setter
@NoArgsConstructor
public class AttributeHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private AttributeEntity attribute;

    // redundant columns kept for compatibility and simpler querying
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private AssetEntity asset;

    @Column(length = 128, nullable = false)
    private String name;

    @Column(length = 1024)
    private String value;

    @Column(name = "value_type", length = 32)
    private String valueType;

    @Column(name = "changed_at")
    private Instant changedAt;

    public AttributeHistoryEntity(AttributeEntity attribute, String value, String valueType, Instant changedAt) {
        this.attribute = attribute;
        this.asset = attribute != null ? attribute.getAsset() : null;
        this.name = attribute != null ? attribute.getName() : null;
        this.value = value;
        this.valueType = valueType;
        this.changedAt = changedAt;
    }
}
