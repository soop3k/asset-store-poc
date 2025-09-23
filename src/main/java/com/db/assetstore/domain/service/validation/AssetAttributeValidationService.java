package com.db.assetstore.domain.service.validation;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.json.AssetJsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

/**
 * Encapsulates attribute validation so higher layers don't depend on JsonNode or parsing details.
 * Provides clear steps for asset creation: parse (via factory), validate (here), save (service/repo).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class AssetAttributeValidationService {
    private final AssetJsonFactory factory;
    private final AssetTypeValidator typeValidator;

    /**
     * Validate flat JSON payload: {"type":"CRE", ...attributes...}
     * - Build Asset via Jackson
     * - Ensure type is supported
     * - Validate attributes against optional schema
     */
    public void validateJson(String json) {
        Objects.requireNonNull(json, "json");
        Asset asset = factory.fromJson(json);
        AssetType type = asset.getType();
        typeValidator.ensureSupported(type);
        Collection<AttributeValue<?>> attrs = asset.getAttributesFlat();
        typeValidator.validateAttributes(type, attrs);
        log.debug("Validated attributes from flat payload for type={}", type);
    }
}
