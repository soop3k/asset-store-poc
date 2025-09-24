package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PatchAssetCommandFactory {

    public PatchAssetCommand create(AssetCommandContext context) {
        Objects.requireNonNull(context, "context");
        var request = Objects.requireNonNull(context.patchRequest(), "patchRequest");

        return PatchAssetCommand.builder()
                .assetId(context.assetId())
                .status(request.getStatus())
                .subtype(request.getSubtype())
                .notionalAmount(request.getNotionalAmount())
                .year(request.getYear())
                .description(request.getDescription())
                .currency(request.getCurrency())
                .attributes(context.attributes())
                .build();
    }
}
