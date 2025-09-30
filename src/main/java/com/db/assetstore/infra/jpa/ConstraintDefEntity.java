package com.db.assetstore.infra.jpa;

import com.db.assetstore.domain.service.type.ConstraintDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
