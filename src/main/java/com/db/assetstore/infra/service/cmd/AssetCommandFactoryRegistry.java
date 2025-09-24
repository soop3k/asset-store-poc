package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetDeleteRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AssetCommandFactoryRegistry {

    private final AttributeJsonReader attributeJsonReader;
    private final CreateAssetCommandFactory createFactory;
    private final PatchAssetCommandFactory patchFactory;
    private final DeleteAssetCommandFactory deleteFactory;

    public AssetCommandFactoryRegistry(AttributeJsonReader attributeJsonReader,
                                       CreateAssetCommandFactory createFactory,
                                       PatchAssetCommandFactory patchFactory,
                                       DeleteAssetCommandFactory deleteFactory) {
        this.attributeJsonReader = Objects.requireNonNull(attributeJsonReader, "attributeJsonReader");
        this.createFactory = Objects.requireNonNull(createFactory, "createFactory");
        this.patchFactory = Objects.requireNonNull(patchFactory, "patchFactory");
        this.deleteFactory = Objects.requireNonNull(deleteFactory, "deleteFactory");
    }

    public CreateAssetCommand createCreateCommand(AssetCreateRequest request) {
        AssetCommandContext context = AssetCommandContext.forCreate(attributeJsonReader, request);
        return createFactory.create(context);
    }

    public PatchAssetCommand createPatchCommand(AssetType assetType, String assetId, AssetPatchRequest request) {
        AssetCommandContext context = AssetCommandContext.forPatch(attributeJsonReader, assetType, assetId, request);
        return patchFactory.create(context);
    }

    public PatchAssetCommand createPatchCommand(AssetType assetType, AssetPatchRequest request) {
        AssetCommandContext context = AssetCommandContext.forPatch(attributeJsonReader, assetType, request);
        return patchFactory.create(context);
    }

    public DeleteAssetCommand createDeleteCommand(String assetId, AssetDeleteRequest request) {
        return deleteFactory.create(assetId, request);
    }
}
