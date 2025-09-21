package com.db.assetstore.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Column(name = "attr_value", length = 1024)
    private String value;

    @Column(name = "value_type", length = 32)
    private String valueType;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeHistoryEntity> history = new ArrayList<>();

    public AttributeEntity(AssetEntity asset, String name, String value, String valueType, Instant updatedAt) {
        this.asset = asset;
        this.name = name;
        this.value = value;
        this.valueType = valueType;
        this.updatedAt = updatedAt;
        addHistory(value, valueType, updatedAt);
    }

    public void updateValue(String value, String valueType, Instant when) {
        // Only record history when something actually changed (value or type)
        boolean sameValue = Objects.equals(this.value, value);
        boolean sameType = Objects.equals(this.valueType, valueType);
        if (sameValue && sameType) {
            return; // no change -> no history entry
        }
        this.value = value;
        this.valueType = valueType;
        this.updatedAt = when;
        addHistory(value, valueType, when);
    }

    public void addHistory(String value, String valueType, Instant when) {
        if (log.isDebugEnabled()) {
            String assetId = asset != null ? asset.getId() : null;
            log.debug("Attribute history added: assetId={}, name={}, valueType={}, when={}", assetId, name, valueType, when);
        }
        this.history.add(new AttributeHistoryEntity(this, value, valueType, when));
    }
}
