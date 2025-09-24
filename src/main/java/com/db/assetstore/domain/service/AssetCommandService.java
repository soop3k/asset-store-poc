package com.db.assetstore.domain.service;

import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.PatchAssetLinkCommand;

public interface AssetCommandService {
    String create(CreateAssetCommand command);
    void update(PatchAssetCommand command);
    String createLink(CreateAssetLinkCommand command);
    void deleteLink(DeleteAssetLinkCommand command);
    void patchLink(PatchAssetLinkCommand command);
}
