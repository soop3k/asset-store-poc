package com.db.assetstore.domain.service.cmd.factory;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetDeleteRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AssetCommandFactoryRegistry {

    private final CreateAssetCommandFactory createFactory;
    private final PatchAssetCommandFactory patchFactory;
    private final DeleteAssetCommandFactory deleteFactory;

    public AssetCommandFactoryRegistry(@NonNull CreateAssetCommandFactory createFactory,
                                       @NonNull PatchAssetCommandFactory patchFactory,
                                       @NonNull DeleteAssetCommandFactory deleteFactory) {
        this.createFactory = createFactory;
        this.patchFactory = patchFactory;
        this.deleteFactory = deleteFactory;
    }

    public CreateAssetCommand createCreateCommand(AssetCreateRequest request) {
        return createFactory.createCommand(request);
    }

    public PatchAssetCommand createPatchCommand(AssetType type, String id, AssetPatchRequest request) {
        return patchFactory.createCommand(type, id, request);
    }

    public PatchAssetCommand createPatchCommand(AssetType type, @NonNull AssetPatchRequest request) {
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
