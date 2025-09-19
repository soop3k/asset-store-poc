package com.db.assetstore.api;

import com.db.assetstore.model.Asset;
import com.db.assetstore.search.SearchCriteria;
import com.db.assetstore.service.AssetService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAsset(@RequestBody String jsonBody) {
        String id = assetService.addAssetFromJson(jsonBody);
        return ResponseEntity.ok(id);
    }

    // Type-specific endpoint example: Commercial Real Estate (CRE)
    @PostMapping(path = "/cre", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAssetCre(@RequestBody String jsonBody) {
        String id = assetService.addAssetFromJson(com.db.assetstore.AssetType.CRE, jsonBody);
        return ResponseEntity.ok(id);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Asset>> listAssets() {
        SearchCriteria criteria = SearchCriteria.builder().build();
        List<Asset> assets = assetService.search(criteria);
        return ResponseEntity.ok(assets);
    }
}
