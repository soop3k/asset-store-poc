package com.db.assetstore.infra.jpa;

import com.db.assetstore.domain.model.type.AttributeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.db.assetstore.domain.model.asset.AssetType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asset_attribute_def")
@Getter
@Setter
@NoArgsConstructor
public class AttributeDefEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private AssetType type;

    @Column(length = 128, nullable = false)
    private String name;

    @Column(name = "value_type", length = 32, nullable = false)
    private AttributeType valueType;

    @Column(name = "required", nullable = false)
    private boolean required = false;

    @OneToMany(mappedBy = "attributeDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConstraintDefEntity> constraints = new ArrayList<>();

    public AttributeDefEntity(AssetType type, String name, AttributeType valueType, boolean required) {
        this.type = type;
        this.name = name;
        this.valueType = valueType;
        this.required = required;
    }

}
