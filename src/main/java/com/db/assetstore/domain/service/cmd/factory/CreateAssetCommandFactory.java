package com.db.assetstore.domain.service.cmd.factory;

import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class CreateAssetCommandFactory {

    private final AttributeJsonReader attributeJsonReader;

    public CreateAssetCommandFactory(@NonNull AttributeJsonReader attributeJsonReader) {
        this.attributeJsonReader = attributeJsonReader;
    }

    public CreateAssetCommand createCommand(@NonNull AssetCreateRequest request) {
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
                .executedBy(request.executedBy())
                .requestTime(Instant.now())
                .build();
    }
}
