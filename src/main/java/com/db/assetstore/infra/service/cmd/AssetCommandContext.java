package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;

/**
 * Shared context used by asset command factories to access request metadata and
 * lazily parse attribute payloads using the {@link AttributeJsonReader}.
 */
public final class AssetCommandContext {

    private final AttributeJsonReader attributeJsonReader;
    private final AssetType assetType;
    private final JsonNode attributePayload;
    private final AssetCreateRequest createRequest;
    private final AssetPatchRequest patchRequest;
    private final String assetId;

    private List<AttributeValue<?>> attributes;

    private AssetCommandContext(AttributeJsonReader attributeJsonReader,
                                AssetType assetType,
                                JsonNode attributePayload,
                                AssetCreateRequest createRequest,
                                AssetPatchRequest patchRequest,
                                String assetId) {
        this.attributeJsonReader = Objects.requireNonNull(attributeJsonReader, "attributeJsonReader");
        this.assetType = Objects.requireNonNull(assetType, "assetType");
        this.attributePayload = attributePayload;
        this.createRequest = createRequest;
        this.patchRequest = patchRequest;
        this.assetId = assetId;
    }

    public static AssetCommandContext forCreate(AttributeJsonReader reader, AssetCreateRequest request) {
        Objects.requireNonNull(request, "request");
        return new AssetCommandContext(reader, request.type(), request.attributes(), request, null, request.id());
    }

    public static AssetCommandContext forPatch(AttributeJsonReader reader,
                                               AssetType assetType,
                                               String assetId,
                                               AssetPatchRequest request) {
        Objects.requireNonNull(assetType, "assetType");
        Objects.requireNonNull(request, "request");
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
        return new AssetCommandContext(reader, assetType, request.getAttributes(), null, request, assetId);
    }

    public static AssetCommandContext forPatch(AttributeJsonReader reader,
                                               AssetType assetType,
                                               AssetPatchRequest request) {
        Objects.requireNonNull(request, "request");
        String id = request.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Patch request must contain an asset id");
        }
        return forPatch(reader, assetType, id, request);
    }

    public AssetType assetType() {
        return assetType;
    }

    public String assetId() {
        return assetId;
    }

    public AssetCreateRequest createRequest() {
        return createRequest;
    }

    public AssetPatchRequest patchRequest() {
        return patchRequest;
    }

    /**
     * Lazily parses the attributes payload using the schema-backed attribute reader.
     *
     * @return immutable list of parsed attribute values
     */
    public List<AttributeValue<?>> attributes() {
        if (attributes == null) {
            attributes = attributePayload == null
                    ? List.of()
                    : List.copyOf(attributeJsonReader.read(assetType, attributePayload));
        }
        return attributes;
    }
}
