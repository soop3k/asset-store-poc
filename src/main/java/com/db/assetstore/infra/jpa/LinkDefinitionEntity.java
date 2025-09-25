package com.db.assetstore.infra.jpa;

import com.db.assetstore.domain.model.link.LinkCardinality;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "link_definition",
       uniqueConstraints = @UniqueConstraint(name = "uk_link_definition_type_subtype",
               columnNames = {"entity_type", "entity_subtype"}))
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
