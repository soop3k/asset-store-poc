package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AssetCommandFactoryRegistry {

    private final CreateAssetCommandFactory createFactory;
    private final PatchAssetCommandFactory patchFactory;

    public AssetCommandFactoryRegistry(CreateAssetCommandFactory createFactory, PatchAssetCommandFactory patchFactory) {
        this.createFactory = createFactory;
        this.patchFactory = patchFactory;
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
}
