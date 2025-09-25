package com.db.assetstore.domain.service.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

        try {
            List<String> names = getTransformFileNames();
            Collections.sort(names);
            log.info("Discovered transforms ({}): {}", names.size(), names);
        } catch (Exception e) {
            log.warn("Failed to scan transforms: {}", e.getMessage());
        }
    }

    private static List<String> getTransformFileNames() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:transforms/**/*.jslt");

        return Arrays.stream(resources)
                .map(Resource::getFilename)
                .filter(Objects::nonNull)
                .map(filename -> filename.substring(0, filename.lastIndexOf('.')))
                .collect(Collectors.toList());
    }
}
