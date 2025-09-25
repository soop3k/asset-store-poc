package com.db.assetstore.domain.service.link.cmd.factory;

import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.infra.api.dto.AssetLinkCreateRequest;
import com.db.assetstore.infra.api.dto.AssetLinkDeleteRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AssetLinkCommandFactoryRegistry {

    private final CreateAssetLinkCommandFactory createFactory;
    private final DeleteAssetLinkCommandFactory deleteFactory;

    public AssetLinkCommandFactoryRegistry(CreateAssetLinkCommandFactory createFactory,
                                           DeleteAssetLinkCommandFactory deleteFactory) {
        this.createFactory = Objects.requireNonNull(createFactory, "createFactory");
        this.deleteFactory = Objects.requireNonNull(deleteFactory, "deleteFactory");
    }

    public CreateAssetLinkCommand createCreateCommand(String assetId, AssetLinkCreateRequest request) {
        return createFactory.createCommand(assetId, request);
    }

    public DeleteAssetLinkCommand createDeleteCommand(String assetId, AssetLinkDeleteRequest request) {
        return deleteFactory.createCommand(assetId, request);
    }
}
