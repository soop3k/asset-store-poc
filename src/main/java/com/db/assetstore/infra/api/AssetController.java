package com.db.assetstore.infra.api;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AssetJsonFactory;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.AssetQueryService;
import com.db.assetstore.domain.service.validation.AssetAttributeValidationService;
import com.db.assetstore.infra.mapper.AssetCreateMapper;
import com.db.assetstore.infra.mapper.AssetPatchMapper;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/assets")
public class AssetController {
    private static final Logger log = LoggerFactory.getLogger(AssetController.class);
    private final AssetQueryService assetQueryService;
    private final AssetCommandService commandService;
    private final AssetCreateMapper createMapper;
    private final AssetPatchMapper patchMapper;

    private final AssetJsonFactory assetJsonFactory = new AssetJsonFactory();
    private final AssetAttributeValidationService validationService = new AssetAttributeValidationService();

    public AssetController(AssetQueryService assetQueryService, AssetCommandService commandService, AssetCreateMapper createMapper, AssetPatchMapper patchMapper) {
        this.assetQueryService = assetQueryService;
        this.commandService = commandService;
        this.createMapper = createMapper;
        this.patchMapper = patchMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAsset(@RequestBody AssetCreateRequest request) {
        log.info("Creating asset");
        var asset = createMapper.toAsset(request);
        String id = commandService.create(asset).id();
        log.debug("Created asset id={}", id);
        return ResponseEntity.ok(id);
    }

    @PostMapping(path = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> addAssetsBulk(@RequestBody String jsonArrayBody) {
        log.info("creating assets in bulk");
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonArrayBody);
            if (node == null || !node.isArray()) {
                throw new IllegalArgumentException("Expected JSON array of assets");
            }
            List<String> ids = new ArrayList<>();
            for (JsonNode item : node) {
                validationService.validateEnvelope(item);
                Asset asset = assetJsonFactory.fromJson(item);
                ids.add(commandService.create(asset).id());
            }
            log.debug("Created {} assets", ids.size());
            return ResponseEntity.ok(ids);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON array payload: " + e.getMessage(), e);
        }
    }

    // Type-specific endpoint example: Commercial Real Estate (CRE)
    @PostMapping(path = "/cre", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAssetCre(@RequestBody String jsonBody) {
        log.info("HTTP POST /assets/cre - creating CRE asset");
        validationService.validateForType(AssetType.CRE, jsonBody);
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
    public ResponseEntity<Void> updateAsset(@PathVariable("id") String id, @RequestBody AssetPatchRequest request) {
        log.info("HTTP PUT /assets/{} - updating asset", id);
        var current = assetQueryService.get(new AssetId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset %s not found".formatted(id)));

        AssetPatch patch = patchMapper.toDomain(current.getType(), request);
        commandService.update(AssetId.of(id), patch);
        return ResponseEntity.noContent().build();
    }
}
