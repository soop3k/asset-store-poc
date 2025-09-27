package com.db.assetstore.infra.jpa;

import com.db.assetstore.domain.service.type.ConstraintDefinition;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 64, nullable = false)
    private ConstraintDefinition.Rule rule;

    @Column(name = "rule_value", length = 2048)
    private String value;

    public ConstraintDefEntity(AttributeDefEntity attributeDefinition, ConstraintDefinition.Rule rule, String value) {
        this.attributeDefinition = attributeDefinition;
        this.rule = rule;
        this.value = value;
    }
}
