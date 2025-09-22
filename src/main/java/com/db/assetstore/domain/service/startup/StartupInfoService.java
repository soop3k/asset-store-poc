package com.db.assetstore.domain.service.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;

/**
 * Logs startup information previously handled by a CommandLineRunner.
 * Now implemented as a service that reacts to ApplicationReadyEvent.
 */
@Service
public class StartupInfoService {
    private static final Logger log = LoggerFactory.getLogger(StartupInfoService.class);
    private final AttributeDefinitionsBootstrapService bootstrapService;

    public StartupInfoService(AttributeDefinitionsBootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        // First, bootstrap attribute definitions into DB
        try {
            bootstrapService.bootstrap();
            log.info("Attribute definitions bootstrap completed");
        } catch (RuntimeException ex) {
            log.warn("Failed to bootstrap attribute definitions: {}", ex.getMessage());
        }

        // Log supported types discovered from schemas
        TypeSchemaRegistry reg = TypeSchemaRegistry.getInstance();
        log.info("Supported asset types (schemas found): {}", reg.supportedTypes());

        // Log available JSLT transforms on classpath
        try {
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
            Collections.sort(names);
            log.info("Discovered JSLT transforms ({}): {}", names.size(), names);
        } catch (Exception e) {
            log.warn("Failed to scan JSLT transforms: {}", e.getMessage());
        }
    }
}
