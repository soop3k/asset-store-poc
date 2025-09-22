package com.db.assetstore.web;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.domain.service.AssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assets")
public class AssetController {
    private static final Logger log = LoggerFactory.getLogger(AssetController.class);
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAsset(@RequestBody String jsonBody) {
        log.info("HTTP POST /assets - creating asset");
        String id = assetService.addAssetFromJson(jsonBody);
        log.debug("Created asset id={}", id);
        return ResponseEntity.ok(id);
    }

    @PostMapping(path = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> addAssetsBulk(@RequestBody String jsonArrayBody) {
        log.info("HTTP POST /assets/bulk - creating assets in bulk");
        List<String> ids = assetService.addAssetsFromJsonArray(jsonArrayBody);
        log.debug("Created {} assets", ids.size());
        return ResponseEntity.ok(ids);
    }

    // Type-specific endpoint example: Commercial Real Estate (CRE)
    @PostMapping(path = "/cre", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAssetCre(@RequestBody String jsonBody) {
        log.info("HTTP POST /assets/cre - creating CRE asset");
        String id = assetService.addAssetFromJson(AssetType.CRE, jsonBody);
        log.debug("Created CRE asset id={}", id);
        return ResponseEntity.ok(id);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Asset>> listAssets() {
        log.info("HTTP GET /assets - listing assets");
        SearchCriteria criteria = SearchCriteria.builder().build();
        List<Asset> assets = assetService.search(criteria);
        log.debug("Returned {} assets", assets.size());
        return ResponseEntity.ok(assets);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Asset> getAsset(@PathVariable("id") String id) {
        return assetService.getAsset(id)
                .map(a -> {
                    try {
                        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(a);
                        log.info("[DEBUG_LOG] GET /assets/{} -> {}", id, json);
                    } catch (Exception ignore) {}
                    return ResponseEntity.ok(a);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Unified asset update: updates both common fields and type-specific attributes
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateAsset(@PathVariable("id") String id, @RequestBody String jsonBody) {
        log.info("HTTP PUT /assets/{} - updating asset", id);
        assetService.updateAssetFromJson(id, jsonBody);
        return ResponseEntity.noContent().build();
    }
}
