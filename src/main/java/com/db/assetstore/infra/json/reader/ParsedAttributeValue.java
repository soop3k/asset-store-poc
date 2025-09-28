package com.db.assetstore.infra.json.reader;

import com.fasterxml.jackson.databind.JsonNode;

record ParsedAttributeValue(String name, JsonNode node) {
}
