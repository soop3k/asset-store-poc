package com.db.assetstore.infra.api;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.AssetQueryService;
import com.db.assetstore.domain.service.link.cmd.factory.AssetLinkCommandFactory;
import com.db.assetstore.infra.api.dto.AssetLinkCreateRequest;
import com.db.assetstore.infra.api.dto.AssetLinkPatchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assets/{assetId}/links")
@RequiredArgsConstructor
public class AssetLinkController {

    private final AssetCommandService commandService;
    private final AssetQueryService queryService;
    private final AssetLinkCommandFactory commandFactory;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> create(@PathVariable("assetId") String assetId,
                                         @RequestBody AssetLinkCreateRequest request) {
        var command = commandFactory.createCommand(assetId, request);
        String id = commandService.createLink(command);
        return ResponseEntity.ok(id);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AssetLink>> list(@PathVariable("assetId") String assetId,
                                                @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly) {
        List<AssetLink> links = queryService.findLinksByAsset(assetId, activeOnly);
        return ResponseEntity.ok(links);
    }

    @DeleteMapping(path = "/{linkId}")
    public ResponseEntity<Void> delete(@PathVariable("assetId") String assetId,
                                       @PathVariable("linkId") String linkId,
                                       @RequestParam(name = "requestedBy", required = false) String requestedBy) {
        var command = commandFactory.deleteCommand(assetId, linkId, requestedBy);
        commandService.deleteLink(command);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{linkId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> patch(@PathVariable("assetId") String assetId,
                                      @PathVariable("linkId") String linkId,
                                      @RequestBody AssetLinkPatchRequest request) {
        var command = commandFactory.patchCommand(assetId, linkId, request);
        commandService.patchLink(command);
        return ResponseEntity.noContent().build();
    }
}
