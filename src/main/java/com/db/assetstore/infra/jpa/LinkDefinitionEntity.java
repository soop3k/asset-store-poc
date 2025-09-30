package com.db.assetstore.infra.jpa;

import com.db.assetstore.domain.model.link.LinkCardinality;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "link_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LinkDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", length = 64, nullable = false)
    private String entityType;

    @Column(name = "entity_subtype", length = 64, nullable = false)
    private String entitySubtype;

    @Enumerated(EnumType.STRING)
    @Column(name = "cardinality", length = 32, nullable = false)
    private LinkCardinality cardinality;

    @Column(name = "active", nullable = false)
    private boolean active;
}
