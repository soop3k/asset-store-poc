package com.db.assetstore.infra.api;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.link.AssetLinkQueryService;
import com.db.assetstore.domain.service.link.cmd.factory.AssetLinkCommandFactoryRegistry;
import com.db.assetstore.infra.api.dto.AssetLinkCreateRequest;
import com.db.assetstore.infra.api.dto.AssetLinkDeleteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/assets/{assetId}/links")
public class AssetLinkController {

    private static final Logger log = LoggerFactory.getLogger(AssetLinkController.class);

    private final AssetLinkQueryService queryService;
    private final AssetCommandService commandService;
    private final AssetLinkCommandFactoryRegistry commandFactoryRegistry;

    public AssetLinkController(AssetLinkQueryService queryService,
                               AssetCommandService commandService,
                               AssetLinkCommandFactoryRegistry commandFactoryRegistry) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.commandFactoryRegistry = commandFactoryRegistry;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AssetLink>> listLinks(@PathVariable("assetId") String assetId,
                                                     @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive) {
        log.info("Listing links for asset {} (includeInactive={})", assetId, includeInactive);
        List<AssetLink> links = includeInactive
                ? queryService.findLinks(assetId, true)
                : queryService.findActiveLinks(assetId);
        return ResponseEntity.ok(links);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> createLink(@PathVariable("assetId") String assetId,
                                           @RequestBody AssetLinkCreateRequest request) {
        log.info("Creating link for asset {}", assetId);
        try {
            CommandResult<Long> result = commandService.execute(
                    commandFactoryRegistry.createCreateCommand(assetId, request));
            if (!result.success()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(result.result());
        } catch (IllegalStateException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteLink(@PathVariable("assetId") String assetId,
                                           @RequestBody AssetLinkDeleteRequest request) {
        log.info("Deleting link for asset {}", assetId);
        try {
            commandService.execute(commandFactoryRegistry.createDeleteCommand(assetId, request));
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
