package com.db.assetstore.domain.service.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import com.db.assetstore.domain.service.type.TypeSchemaRegistry;

@Slf4j
@Service
@RequiredArgsConstructor
public class BootstrapService {
    private final AttributeBootstrapService bootstrapService;
    private final TypeSchemaRegistry typeSchemaRegistry;


    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            bootstrapService.bootstrap();
            log.info("Attribute definitions bootstrap completed");
        } catch (RuntimeException ex) {
            log.warn("Failed to bootstrap attribute definitions: {}", ex.getMessage());
        }

        log.info("Supported asset types (schemas found): {}", typeSchemaRegistry.supportedTypes());
    }
}
