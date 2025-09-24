package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class CreateAssetCommandFactory {

    private final AttributeJsonReader attributeJsonReader;

    public CreateAssetCommandFactory(AttributeJsonReader attributeJsonReader) {
        this.attributeJsonReader = Objects.requireNonNull(attributeJsonReader, "attributeJsonReader");
    }

    public CreateAssetCommand create(AssetCreateRequest request) {
        Objects.requireNonNull(request, "request");

        List<AttributeValue<?>> attributes = request.attributes() == null
                ? List.of()
                : List.copyOf(attributeJsonReader.read(request.type(), request.attributes()));

        return CreateAssetCommand.builder()
                .id(request.id())
                .type(request.type())
                .status(request.status())
                .subtype(request.subtype())
                .notionalAmount(request.notionalAmount())
                .year(request.year())
                .description(request.description())
                .currency(request.currency())
                .attributes(attributes)
                .build();
    }
}
