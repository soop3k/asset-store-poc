package com.db.assetstore.infra.api;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.AssetQueryService;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchItemRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.db.assetstore.infra.mapper.AssetRequestMapper;
import com.db.assetstore.domain.json.AssetJsonFactory;
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
    private final AssetRequestMapper requestMapper;
    private final AssetJsonFactory assetJsonFactory;

    public AssetController(AssetQueryService assetQueryService, AssetCommandService commandService, AssetRequestMapper requestMapper, AssetJsonFactory assetJsonFactory) {
        this.assetQueryService = assetQueryService;
        this.commandService = commandService;
        this.requestMapper = requestMapper;
        this.assetJsonFactory = assetJsonFactory;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAsset(@RequestBody AssetCreateRequest request) {
        log.info("Creating asset");
        String id = commandService.create(requestMapper.toCreateCommand(request, "api", null)).id();
        log.debug("Created asset id={}", id);
        return ResponseEntity.ok(id);
    }

    @PostMapping(path = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> addAssetsBulk(@RequestBody List<AssetCreateRequest> requests) {
        log.info("HTTP POST /assets/bulk - creating {} assets", requests == null ? 0 : requests.size());
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<String> ids = requests.stream()
                .map(req -> requestMapper.toCreateCommand(req, "api", null))
                .map(cmd -> commandService.create(cmd).id())
                .toList();
        log.debug("Created {} assets", ids.size());
        return ResponseEntity.ok(ids);
    }

    // Type-specific endpoint example: Commercial Real Estate (CRE)
    @PostMapping(path = "/cre", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAssetCre(@RequestBody String jsonBody) {
        log.info("HTTP POST /assets/cre - creating CRE asset");
        String id = commandService.create(assetJsonFactory.fromJsonForType(AssetType.CRE, jsonBody)).id();
        log.debug("Created CRE asset id={}", id);
        return ResponseEntity.ok(id);
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
        return assetQueryService.get(new AssetId(id))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Unified asset update: updates both common fields and type-specific attributes
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateAsset(@PathVariable("id") String id, @RequestBody AssetPatchRequest request) {
        log.info("HTTP PUT /assets/{} - updating asset", id);
        var current = assetQueryService.get(new AssetId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset %s not found".formatted(id)));

        var cmd = requestMapper.toPatchCommand(current.getType(), id, request, "api", null);
        commandService.update(cmd);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> patchAsset(@PathVariable("id") String id, @RequestBody AssetPatchRequest request) {
        log.info("HTTP PATCH /assets/{} - patch asset", id);
        var current = assetQueryService.get(new AssetId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset %s not found".formatted(id)));
        var cmd = requestMapper.toPatchCommand(current.getType(), id, request, "api", null);
        commandService.update(cmd);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> patchAssetsBulk(@RequestBody List<AssetPatchItemRequest> requests) {
        log.info("HTTP PATCH /assets/bulk - patch {} assets", requests == null ? 0 : requests.size());
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        for (AssetPatchItemRequest item : requests) {
            var current = assetQueryService.get(new AssetId(item.getId()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset %s not found".formatted(item.getId())));
            var cmd = requestMapper.toPatchCommand(current.getType(), item, "api", null);
            commandService.update(cmd);
        }
        return ResponseEntity.noContent().build();
    }
}
