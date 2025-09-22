package com.db.assetstore.domain.model;

import java.time.Instant;

public record AttributeHistory(String assetId, String name, String value, String valueType, Instant changedAt) {
}
