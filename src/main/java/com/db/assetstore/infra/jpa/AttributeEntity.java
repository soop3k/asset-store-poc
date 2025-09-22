package com.db.assetstore.infra.jpa;

import com.db.assetstore.domain.model.AttributeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asset_attribute", uniqueConstraints = {
        @UniqueConstraint(name = "uk_asset_attr_unique", columnNames = {"asset_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class AttributeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private AssetEntity asset;

    @Column(length = 128, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", length = 32)
    private AttributeType valueType;

    @Column(name = "value_str", length = 1024)
    private String valueStr;

    @Column(name = "value_bool")
    private Boolean valueBool;

    @Column(name = "value_num", precision = 38, scale = 10)
    private BigDecimal valueNum;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeHistoryEntity> history = new ArrayList<>();

    public AttributeEntity(AssetEntity a, String n, String v, Instant when) {
        this.asset=a;
        this.name=n;
        this.updatedAt=when;
        this.valueType=AttributeType.STRING;
        this.valueStr=v;
    }

    public AttributeEntity(AssetEntity a, String n, BigDecimal v, Instant when) {
        this.asset=a;
        this.name=n;
        this.updatedAt=when;
        this.valueType=AttributeType.DECIMAL;
        this.valueNum=v;
    }

    public AttributeEntity(AssetEntity a, String n, Boolean v, Instant when) {
        this.asset=a;
        this.name=n;
        this.updatedAt=when;
        this.valueType=AttributeType.BOOLEAN;
        this.valueBool=v;
    }

    private void addHistory(Instant when) {
        if (log.isDebugEnabled()) {
            String assetId = asset != null ? asset.getId() : null;
            log.debug("Attribute history added: assetId={}, name={}, valueType={}, when={}", assetId, name, valueType, when);
        }
        this.history.add(AttributeHistoryEntity.fromCurrent(this, when));
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
        addHistory(Instant.now());
    }

    @PreUpdate
    protected void onPreUpdate() {
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
        addHistory(Instant.now());
    }
}
