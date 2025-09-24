package com.db.assetstore.infra.jpa.link;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "link_subtype_def")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LinkSubtypeDefinitionEntity {

    @EmbeddedId
    private LinkSubtypeDefinitionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("code")
    @JoinColumn(name = "code", nullable = false)
    private LinkDefinitionEntity definition;
}
