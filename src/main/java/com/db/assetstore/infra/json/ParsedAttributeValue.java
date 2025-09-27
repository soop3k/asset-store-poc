package com.db.assetstore.infra.json;

import com.fasterxml.jackson.databind.JsonNode;

record ParsedAttributeValue(String name, JsonNode node) {
}
