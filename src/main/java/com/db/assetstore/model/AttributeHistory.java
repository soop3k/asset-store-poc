package com.db.assetstore.model;

import java.time.Instant;

public record AttributeHistory(String assetId, String name, String value, String valueType, Instant changedAt) {
}
