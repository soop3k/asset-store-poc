package com.db.assetstore.infra.api;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.service.AssetQueryService;
import com.db.assetstore.domain.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/events")
public class EventController {
    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final AssetQueryService assetQueryService;
    private final EventService eventService;

    public EventController(AssetQueryService assetQueryService, EventService eventService) {
        this.assetQueryService = assetQueryService;
        this.eventService = eventService;
    }

    @GetMapping(path = "/{assetId}/{eventName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateEvent(@PathVariable("assetId") String assetId,
                                                @PathVariable("eventName") String eventName) {
        log.info("HTTP GET /events/{}/{} - generating event", assetId, eventName);
        Optional<Asset> assetOpt = assetQueryService.get(new AssetId(assetId));
        if (assetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String json = eventService.generate(eventName, assetOpt.get());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
    }
}
