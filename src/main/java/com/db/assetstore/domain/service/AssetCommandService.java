package com.db.assetstore.domain.service;

import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;

public interface AssetCommandService {
    String create(CreateAssetCommand command);
    void update(PatchAssetCommand command);
}
