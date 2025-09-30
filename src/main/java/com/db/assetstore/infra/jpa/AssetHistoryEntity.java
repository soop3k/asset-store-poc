package com.db.assetstore.infra.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "asset_history")
@Getter
@Setter
@NoArgsConstructor
public class AssetHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private AssetEntity asset;

    @Column(name = "status", length = 64)
    private String status;

    @Column(name = "subtype", length = 64)
    private String subtype;

    @Column(name = "status_effective_time")
    private Instant statusEffectiveTime;

    @Column(name = "modified_at")
    private Instant modifiedAt;

    @Column(name = "modified_by", length = 64)
    private String modifiedBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "notional_amount", precision = 19, scale = 4)
    private BigDecimal notionalAmount;

    @Column(name = "asset_year")
    private Integer year;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "currency", length = 8)
    private String currency;

    @Column(name = "wh", length = 64)
    private String wh;

    @Column(name = "source_system_name", length = 64)
    private String sourceSystemName;

    @Column(name = "external_reference", length = 128)
    private String externalReference;

    @Column(name = "deleted", nullable = false)
    private int deleted;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public AssetHistoryEntity(@NonNull AssetEntity asset, @NonNull Instant when) {
        this.asset = asset;
        this.status = asset.getStatus();
        this.subtype = asset.getSubtype();
        this.statusEffectiveTime = asset.getStatusEffectiveTime();
        this.modifiedAt = asset.getModifiedAt();
        this.modifiedBy = asset.getModifiedBy();
        this.createdAt = asset.getCreatedAt();
        this.createdBy = asset.getCreatedBy();
        this.notionalAmount = asset.getNotionalAmount();
        this.year = asset.getYear();
        this.description = asset.getDescription();
        this.currency = asset.getCurrency();
        this.wh = asset.getWh();
        this.sourceSystemName = asset.getSourceSystemName();
        this.externalReference = asset.getExternalReference();
        this.deleted = asset.getDeleted();
        this.changedAt = when;
    }
}
