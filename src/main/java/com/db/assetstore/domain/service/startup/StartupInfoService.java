package com.db.assetstore.domain.service.startup;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;

/**
 * Logs startup information previously handled by a CommandLineRunner.
 * Now implemented as a service that reacts to ApplicationReadyEvent.
 */
@Service
@RequiredArgsConstructor
public class StartupInfoService {
    private static final Logger log = LoggerFactory.getLogger(StartupInfoService.class);
    private final AttributeDefinitionsBootstrapService bootstrapService;
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
            log.info("Discovered JSLT transforms ({}): {}", names.size(), names);
        } catch (Exception e) {
            log.warn("Failed to scan JSLT transforms: {}", e.getMessage());
        }
    }

    private static List<String> getTransformFileNames() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:transforms/**/*.jslt");
        List<String> names = new ArrayList<>();
        for (Resource r : resources) {
            try {
                String desc = r.getURL().toString();
                int idx = desc.indexOf("transforms/");
                if (idx >= 0) {
                    names.add(desc.substring(idx));
                } else if (r.getFilename() != null) {
                    names.add(r.getFilename());
                }
            } catch (Exception ignored) {
                if (r.getFilename() != null) names.add(r.getFilename());
            }
        }
        return names;
    }
}
