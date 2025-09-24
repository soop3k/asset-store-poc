package com.db.assetstore.infra.jpa.link;

import com.db.assetstore.domain.model.link.LinkCardinality;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "link_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LinkDefinitionEntity {

    @Id
    @Column(name = "code", length = 64, nullable = false)
    private String code;

    @Column(name = "entity_type", length = 64, nullable = false)
    private String entityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "cardinality", length = 32, nullable = false)
    private LinkCardinality cardinality;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "link_subtype_def", joinColumns = @JoinColumn(name = "code"))
    @Column(name = "entity_type", length = 64, nullable = false)
    private Set<String> allowedEntityTypes = new HashSet<>();
}
