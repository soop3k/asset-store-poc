package com.db.assetstore.infra.api;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.AssetQueryService;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetDeleteRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.db.assetstore.domain.service.cmd.factory.AssetCommandFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/assets")
public class AssetController {
    private static final Logger log = LoggerFactory.getLogger(AssetController.class);
    private final AssetQueryService assetQueryService;
    private final AssetCommandService commandService;
    private final AssetCommandFactoryRegistry commandFactoryRegistry;

    public AssetController(AssetQueryService assetQueryService,
                           AssetCommandService commandService,
                           AssetCommandFactoryRegistry commandFactoryRegistry) {
        this.assetQueryService = assetQueryService;
        this.commandService = commandService;
        this.commandFactoryRegistry = commandFactoryRegistry;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAsset(@RequestBody AssetCreateRequest request) {
        log.info("Creating asset");
        String id = commandService.create(commandFactoryRegistry.createCreateCommand(request));
        log.debug("Created asset id={}", id);
        return ResponseEntity.ok(id);
    }

    @PostMapping(path = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> addAssetsBulk(@RequestBody List<AssetCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.info("HTTP POST /assets/bulk - creating 0 assets");
            return ResponseEntity.ok(List.of());
        }
        log.info("HTTP POST /assets/bulk - creating {} assets", requests.size());
        List<String> ids = requests.stream()
                .map(commandFactoryRegistry::createCreateCommand)
                .map(commandService::create)
                .toList();
        log.debug("Created {} assets", ids.size());
        return ResponseEntity.ok(ids);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Asset>> listAssets() {
        log.info("HTTP GET /assets - listing assets");
        SearchCriteria criteria = SearchCriteria.builder().build();
        List<Asset> assets = assetQueryService.search(criteria);
        log.debug("Returned {} assets", assets.size());
        return ResponseEntity.ok(assets);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Asset> getAsset(@PathVariable("id") String id) {
        return assetQueryService.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Unified asset update: updates both common fields and type-specific attributes
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateAsset(@PathVariable("id") String id, @RequestBody AssetPatchRequest request) {
        log.info("HTTP PUT /assets/{} - updating asset", id);
        var current = assetQueryService.get(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset %s not found".formatted(id)));

        var cmd = commandFactoryRegistry.createPatchCommand(current.getType(), id, request);
        commandService.update(cmd);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> patchAsset(@PathVariable("id") String id, @RequestBody AssetPatchRequest request) {
        log.info("HTTP PATCH /assets/{} - patch asset", id);
        var current = assetQueryService.get(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset %s not found".formatted(id)));
        var cmd = commandFactoryRegistry.createPatchCommand(current.getType(), id, request);
        commandService.update(cmd);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> patchAssetsBulk(@RequestBody List<AssetPatchRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.info("HTTP PATCH /assets/bulk - patch 0 assets");
            return ResponseEntity.noContent().build();
        }
        log.info("HTTP PATCH /assets/bulk - patch {} assets", requests.size());
        for (var item : requests) {
            var current = assetQueryService.get(item.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset %s not found".formatted(item.getId())));
            var cmd = commandFactoryRegistry.createPatchCommand(current.getType(), item);
            commandService.update(cmd);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAsset(@PathVariable("id") String id,
                                            @RequestBody AssetDeleteRequest request) {
        log.info("HTTP DELETE /assets/{} - delete asset", id);
        var cmd = commandFactoryRegistry.createDeleteCommand(id, request);
        commandService.delete(cmd);
        return ResponseEntity.noContent().build();
    }
}
