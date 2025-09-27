package com.db.assetstore.infra.api;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.domain.service.asset.AssetQueryService;
import com.db.assetstore.domain.service.link.AssetLinkQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/assets/{assetId}/links")
public class AssetLinkController {

    private static final Logger log = LoggerFactory.getLogger(AssetLinkController.class);

    private final AssetQueryService assetQueryService;
    private final AssetLinkQueryService assetLinkQueryService;

    public AssetLinkController(AssetQueryService assetQueryService,
                               AssetLinkQueryService assetLinkQueryService) {
        this.assetQueryService = assetQueryService;
        this.assetLinkQueryService = assetLinkQueryService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AssetLink>> listAssetLinks(@PathVariable("assetId") String assetId,
                                                          @RequestParam(value = "includeInactive", defaultValue = "false") boolean includeInactive) {
        log.info("HTTP GET /assets/{}/links - listing asset links", assetId);
        Optional<Asset> asset = assetQueryService.get(assetId);
        if (asset.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<AssetLink> links = assetLinkQueryService.findLinks(assetId, includeInactive);
        String scopeDescription = includeInactive ? "all" : "active";
        log.debug("Returned {} {} links for asset {}", links.size(), scopeDescription, assetId);
        return ResponseEntity.ok(links);
    }
}
