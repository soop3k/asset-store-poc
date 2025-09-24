package com.db.assetstore.infra.jpa.link;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "asset_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AssetLinkEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "asset_id", length = 36, nullable = false)
    private String assetId;

    @Column(name = "link_code", length = 64, nullable = false)
    private String linkCode;

    @Column(name = "link_subtype", length = 64, nullable = false)
    private String linkSubtype;

    @Column(name = "entity_type", length = 64, nullable = false)
    private String entityType;

    @Column(name = "entity_id", length = 128, nullable = false)
    private String entityId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "modified_at")
    private Instant modifiedAt;

    @Column(name = "modified_by", length = 64)
    private String modifiedBy;
}
