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

    @Column(name = "subtype", length = 64, nullable = false)
    private String subtype;

    public String getCode() {
        return code;
    }

    public String getSubtype() {
        return subtype;
    }
}
