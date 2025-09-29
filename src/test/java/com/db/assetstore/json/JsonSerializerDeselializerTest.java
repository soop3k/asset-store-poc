package com.db.assetstore.json;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.model.asset.Asset;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.*;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.infra.json.AssetSerializer;
import com.db.assetstore.infra.json.reader.AttributeJsonReader;
import com.db.assetstore.infra.json.reader.AttributePayloadParser;
import com.db.assetstore.infra.json.reader.AttributeValueAssembler;
import com.db.assetstore.testutil.InMemoryAttributeDefinitionLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.db.assetstore.domain.service.type.ConstraintDefinition.Rule.TYPE;
import static com.db.assetstore.testutil.AttributeTestHelpers.constraint;
import static com.db.assetstore.testutil.AttributeTestHelpers.definition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSerializerDeselializerTest {

    @Test
    void ensureJsonSerializerAndDeserializerAreInSync() throws JsonProcessingException {
        var mapper = new JsonMapperProvider().objectMapper();

        var originalAsset = Asset.builder()
                .id("A-1")
                .type(AssetType.CRE)
                .createdAt(Instant.now())
                .currency("USB")
                .createdBy("test")
                .description("Test asset")
                .modifiedBy("test")
                .modifiedAt(Instant.now())
                .year(2025)
                .status("Asset status")
                .notionalAmount(BigDecimal.ZERO)
                .attributes(
                        AttributesCollection.fromFlat(
                                List.of(AVDate.of("availableSince", Instant.now()),
                                        AVString.of("city", "test"),
                                        AVDecimal.of("area", BigDecimal.valueOf(33.11)),
                                        AVBoolean.of("active", true)
                        )
                )).build();

        var serializer = new AssetSerializer(mapper);
        var payload = serializer.toJson(originalAsset);

        var assetFromJson = mapper.readValue(payload, Asset.class);

        var city = definition(AssetType.CRE, "city", AttributeType.STRING);
        var area = definition(AssetType.CRE, "area", AttributeType.DECIMAL);
        var active = definition(AssetType.CRE, "active", AttributeType.BOOLEAN);
        var availableSince = definition(AssetType.CRE, "availableSince", AttributeType.DATE);

        var registry = InMemoryAttributeDefinitionLoader.builder()
                .withAttribute(city, constraint(city, TYPE))
                .withAttribute(area, constraint(area, TYPE))
                .withAttribute(active, constraint(active, TYPE))
                .withAttribute(availableSince, constraint(availableSince, TYPE))
                .buildRegistry();

        AttributeJsonReader reader = new AttributeJsonReader(
                new AttributePayloadParser(),
                new AttributeValueAssembler(registry)
        );

        AttributesCollection attrFromJson = reader.read(
                assetFromJson.getType(),
                mapper.readTree(payload).get("attributes")
        );

        assertEquals(originalAsset.getId(), assetFromJson.getId());
        assertEquals(originalAsset.getCreatedAt(), assetFromJson.getCreatedAt());
        assertEquals(originalAsset.getCurrency(), assetFromJson.getCurrency());
        assertEquals(originalAsset.getCreatedBy(), assetFromJson.getCreatedBy());
        assertEquals(originalAsset.getDescription(), assetFromJson.getDescription());
        assertEquals(originalAsset.getModifiedBy(), assetFromJson.getModifiedBy());
        assertEquals(originalAsset.getModifiedAt(), assetFromJson.getModifiedAt());
        assertEquals(originalAsset.getYear(), assetFromJson.getYear());
        assertEquals(originalAsset.getStatus(), assetFromJson.getStatus());
        assertEquals(originalAsset.getNotionalAmount(), assetFromJson.getNotionalAmount());

        assertEquals(originalAsset.getAttributes().asList(), attrFromJson.asList());
    }

}
