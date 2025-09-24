package com.db.assetstore.infra.jpa.link;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LinkSubtypeDefinitionId implements Serializable {

    @Column(name = "code", length = 64, nullable = false)
    private String code;

    @Column(name = "entity_type", length = 64, nullable = false)
    private String entityType;

    public String getCode() {
        return code;
    }

    public String getEntityType() {
        return entityType;
    }
}
