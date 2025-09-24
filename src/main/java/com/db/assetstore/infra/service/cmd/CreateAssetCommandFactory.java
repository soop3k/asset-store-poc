package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreateAssetCommandFactory {

    public CreateAssetCommand create(AssetCommandContext context) {
        Objects.requireNonNull(context, "context");
        var request = Objects.requireNonNull(context.createRequest(), "createRequest");

        return CreateAssetCommand.builder()
                .id(request.id())
                .type(request.type())
                .status(request.status())
                .subtype(request.subtype())
                .notionalAmount(request.notionalAmount())
                .year(request.year())
                .description(request.description())
                .currency(request.currency())
                .attributes(context.attributes())
                .build();
    }
}
