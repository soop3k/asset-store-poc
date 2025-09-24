package com.db.assetstore.infra.jpa.link;

import com.db.assetstore.domain.model.link.LinkCardinality;
import jakarta.persistence.*;
import lombok.*;

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
    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LinkSubtypeDefinitionEntity> allowedEntityTypes = new HashSet<>();
}
