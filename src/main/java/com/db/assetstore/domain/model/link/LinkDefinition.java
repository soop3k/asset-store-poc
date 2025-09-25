package com.db.assetstore.domain.model.link;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LinkDefinition {
    Long id;
    String entityType;
    String entitySubtype;
    LinkCardinality cardinality;
    boolean active;
}
