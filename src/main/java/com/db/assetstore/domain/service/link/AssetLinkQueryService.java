package com.db.assetstore.domain.service.link;

import com.db.assetstore.domain.model.link.AssetLink;

import java.util.List;

public interface AssetLinkQueryService {
    List<AssetLink> findActiveLinks(String assetId);
    List<AssetLink> findLinks(String assetId, boolean includeInactive);
}
