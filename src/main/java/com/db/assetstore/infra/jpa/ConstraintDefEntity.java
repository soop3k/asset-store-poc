package com.db.assetstore.infra.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "asset_attribute_constraint_def")
@Getter
@Setter
@NoArgsConstructor
public class ConstraintDefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_def_id", nullable = false)
    private AttributeDefEntity attributeDefinition;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "value", length = 2048)
    private String value;

    public ConstraintDefEntity(AttributeDefEntity attributeDefinition, String name, String value) {
        this.attributeDefinition = attributeDefinition;
        this.name = name;
        this.value = value;
    }
}
