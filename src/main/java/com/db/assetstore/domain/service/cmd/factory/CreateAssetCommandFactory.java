package com.db.assetstore.domain.service.cmd.factory;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.validation.AttributeValidator;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.json.AttributeJsonReader;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class CreateAssetCommandFactory {

    private final AttributeValidator attributeValidator;
    private final AttributeJsonReader attributeJsonReader;

    public CreateAssetCommandFactory(@NonNull AttributeValidator attributeValidator,
                                     @NonNull AttributeJsonReader attributeJsonReader) {
        this.attributeValidator = attributeValidator;
        this.attributeJsonReader = attributeJsonReader;
    }

    public CreateAssetCommand createCommand(@NonNull AssetCreateRequest request) {
        AttributesCollection attributes = attributeJsonReader.read(request.type(), request.attributes());

        attributeValidator.validate(request.type(), attributes);

        List<AttributeValue<?>> attributeValues = attributes.asListView();

        return CreateAssetCommand.builder()
                .id(request.id())
                .type(request.type())
                .status(request.status())
                .subtype(request.subtype())
                .notionalAmount(request.notionalAmount())
                .year(request.year())
                .description(request.description())
                .currency(request.currency())
                .attributes(attributeValues)
                .executedBy(request.executedBy())
                .requestTime(Instant.now())
                .build();
    }
}
