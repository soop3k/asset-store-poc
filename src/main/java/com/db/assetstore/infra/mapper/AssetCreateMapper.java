package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AssetCreateMapper {
    private final AttributeJsonReader attrReader;

    public Asset toAsset(AssetCreateRequest req) {
        List<AttributeValue<?>> avs =
                req.attributes() == null ? List.of() : attrReader.read(req.type(), req.attributes());

        String id = (req.id() != null && !req.id().isBlank()) ? req.id() : UUID.randomUUID().toString();
        Asset asset = new Asset(
                id,
                req.type(),
                Instant.now(),
                AttributesCollection.fromFlat(avs)
        );
        asset.setStatus(req.status());
        asset.setSubtype(req.subtype());
        asset.setNotionalAmount(req.notionalAmount());
        asset.setYear(req.year());
        asset.setDescription(req.description());
        asset.setCurrency(req.currency());
        return asset;
    }
}