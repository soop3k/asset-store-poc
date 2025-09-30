package com.db.assetstore.infra.jpa;

import com.db.assetstore.domain.model.asset.AssetType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AssetEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Version
    @Column(name = "version")
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private AssetType type;

    @Column(name = "status", length = 64)
    private String status;

    @Column(name = "subtype", length = 64)
    private String subtype;

    @Column(name = "status_effective_time")
    private Instant statusEffectiveTime;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "modified_at")
    private Instant modifiedAt;

    @Column(name = "modified_by", length = 64)
    private String modifiedBy;

    @Builder.Default
    @Column(nullable = false)
    private int deleted = 0;

    @Column(name = "notional_amount", precision = 19, scale = 4)
    private BigDecimal notionalAmount;

    @Column(name = "asset_year")
    private Integer year;

    @Column(name = "wh", length = 64)
    private String wh;

    @Column(name = "source_system_name", length = 64)
    private String sourceSystemName;

    @Column(name = "external_reference", length = 128)
    private String externalReference;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "currency", length = 8)
    private String currency;

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeEntity> attributes = new ArrayList<>();

    public AssetEntity(String id, AssetType type, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.createdAt = createdAt;
    }
}
