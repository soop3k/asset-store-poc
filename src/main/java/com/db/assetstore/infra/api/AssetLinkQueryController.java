package com.db.assetstore.infra.api;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.domain.service.AssetQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class AssetLinkQueryController {

    private final AssetQueryService queryService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AssetLink>> findByEntity(@RequestParam("entityType") String entityType,
                                                        @RequestParam("entityId") String entityId,
                                                        @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly) {
        List<AssetLink> links = queryService.findLinksByEntity(entityType, entityId, activeOnly);
        return ResponseEntity.ok(links);
    }
}
