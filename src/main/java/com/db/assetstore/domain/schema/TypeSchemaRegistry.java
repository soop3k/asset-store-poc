package com.db.assetstore.domain.schema;

import com.db.assetstore.AssetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Discovers and keeps mapping of AssetType -> JSON Schema resource path.
 * A type is considered "supported" only if a classpath resource exists at schemas/{TYPE}.schema.json.
 * Adding a new type is as simple as dropping a new schema file with that naming convention.
 */
public final class TypeSchemaRegistry {
    private static final Logger log = LoggerFactory.getLogger(TypeSchemaRegistry.class);
    private static final String SCHEMA_PATH_PATTERN = "schemas/%s.schema.json";

    private static final TypeSchemaRegistry INSTANCE = new TypeSchemaRegistry();

    private final Map<AssetType, String> typeToSchema = new ConcurrentHashMap<>();

    private TypeSchemaRegistry() {
        discover();
    }

    public static TypeSchemaRegistry getInstance() { return INSTANCE; }

    private void discover() {
        ClassLoader cl = TypeSchemaRegistry.class.getClassLoader();
        int found = 0;
        for (AssetType t : AssetType.values()) {
            String path = String.format(SCHEMA_PATH_PATTERN, t.name());
            URL url = cl.getResource(path);
            if (url != null) {
                typeToSchema.put(t, path);
                found++;
            }
        }
        log.info("TypeSchemaRegistry initialized: supported types={} ({} schemas found)", typeToSchema.keySet(), found);
    }


    public Optional<String> getSchemaPath(AssetType type) {
        return Optional.ofNullable(typeToSchema.get(type));
    }

    public Set<AssetType> supportedTypes() {
        return Collections.unmodifiableSet(typeToSchema.keySet());
    }
}
