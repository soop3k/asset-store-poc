package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public final class TypeSchemaRegistry {

    private static final String SCHEMA_PATH_PATTERN = "schemas/types/%s.schema.json";

    private final ObjectMapper om;
    private final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    private final Map<AssetType, Entry> entries = new LinkedHashMap<>();

    public record Entry(String path, JsonNode node, JsonSchema compiled) {}

    public void discover() { rebuild(); }

    public void rebuild() {
        entries.clear();

        ClassLoader cl = TypeSchemaRegistry.class.getClassLoader();
        for (AssetType t : AssetType.values()) {
            String path = String.format(SCHEMA_PATH_PATTERN, t.name());
            try (InputStream is = cl.getResourceAsStream(path)) {
                if (is == null) {
                    continue;
                }
                JsonNode node = om.readTree(is);
                JsonSchema compiled = factory.getSchema(node,
                        SchemaValidatorsConfig.builder().failFast(true).typeLoose(true).build());
                entries.put(t, new Entry(path, node, compiled));
            } catch (Exception e) {
                log.warn("Failed to load/compile schema for {} from {}: {}", t, path, e.getMessage());
            }
        }
        log.info("TypeSchemaRegistry: supported={}", entries.keySet());
    }

    public Set<AssetType> supportedTypes() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    public Optional<JsonNode> getSchemaNode(AssetType type) {
        Entry e = entries.get(type);
        return e == null ? Optional.empty() : Optional.of(e.node());
    }

}
