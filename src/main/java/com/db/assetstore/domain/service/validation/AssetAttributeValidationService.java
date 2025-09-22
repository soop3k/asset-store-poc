package com.db.assetstore.domain.service.validation;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AttributeValue;
import lombok.extern.slf4j.Slf4j;
import com.db.assetstore.domain.json.AssetJsonFactory;

import java.util.Collection;
import java.util.Objects;

/**
 * Encapsulates attribute validation so higher layers don't depend on JsonNode or parsing details.
 * Provides clear steps for asset creation: parse (via factory), validate (here), save (service/repo).
 */
@Slf4j
public final class AssetAttributeValidationService {
    private final AssetJsonFactory factory = new AssetJsonFactory();
    private final AssetTypeValidator typeValidator = new AssetTypeValidator();

    /**
     * Validate flat JSON payload: {"type":"CRE", ...attributes...}
     * - Build Asset via Jackson
     * - Ensure type is supported
     * - Validate attributes against optional schema
     */
    public void validateEnvelope(String json) {
        Objects.requireNonNull(json, "json");
        Asset asset = factory.fromJson(json);
        AssetType type = asset.getType();
        typeValidator.ensureSupported(type);
        Collection<AttributeValue<?>> attrs = asset.getAttributesByName().values();
        typeValidator.validateAttributes(type, attrs);
        log.debug("Validated attributes from flat payload for type={}", type);
    }

    /**
     * Same validation but for a pre-parsed JsonNode (object). Useful for bulk array parsing without re-serialization.
     */
    public void validateEnvelope(com.fasterxml.jackson.databind.JsonNode node) {
        Objects.requireNonNull(node, "node");
        Asset asset = factory.fromJson(node);
        AssetType type = asset.getType();
        typeValidator.ensureSupported(type);
        Collection<AttributeValue<?>> attrs = asset.getAttributesByName().values();
        typeValidator.validateAttributes(type, attrs);
        log.debug("Validated attributes from flat payload for type={}", type);
    }

    /**
     * Validate a type-specific JSON (without wrapper). providedType is a hint for loading definitions.
     */
    public void validateForType(AssetType type, String json) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(json, "json");
        typeValidator.ensureSupported(type);
        Asset asset = factory.fromJsonForType(type, json);
        Collection<AttributeValue<?>> attrs = asset.getAttributesByName().values();
        typeValidator.validateAttributes(type, attrs);
        log.debug("Validated type-specific attributes for type={}", type);
    }
}
