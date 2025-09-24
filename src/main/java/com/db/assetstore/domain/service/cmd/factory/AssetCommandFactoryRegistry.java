package com.db.assetstore.domain.service.cmd.factory;

import com.db.assetstore.AssetType;
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

    private final CreateAssetCommandFactory createFactory;
    private final PatchAssetCommandFactory patchFactory;
    private final DeleteAssetCommandFactory deleteFactory;

    public AssetCommandFactoryRegistry(CreateAssetCommandFactory createFactory,
                                       PatchAssetCommandFactory patchFactory,
                                       DeleteAssetCommandFactory deleteFactory) {
        this.createFactory = Objects.requireNonNull(createFactory, "createFactory");
        this.patchFactory = Objects.requireNonNull(patchFactory, "patchFactory");
        this.deleteFactory = Objects.requireNonNull(deleteFactory, "deleteFactory");
    }

    public CreateAssetCommand createCreateCommand(AssetCreateRequest request) {
        return createFactory.createCommand(request);
    }

    public PatchAssetCommand createPatchCommand(AssetType type, String id, AssetPatchRequest request) {
        return patchFactory.createCommand(type, id, request);
    }

    public PatchAssetCommand createPatchCommand(AssetType type, AssetPatchRequest request) {
        Objects.requireNonNull(request, "request");
        String id = request.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Patch request must contain an asset id");
        }
        return createPatchCommand(type, id, request);
    }

    public DeleteAssetCommand createDeleteCommand(String assetId, AssetDeleteRequest request) {
        return deleteFactory.createCommand(assetId, request);
    }
}
