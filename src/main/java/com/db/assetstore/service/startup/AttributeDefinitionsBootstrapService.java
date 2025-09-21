package com.db.assetstore.service.startup;

import com.db.assetstore.AssetType;
import com.db.assetstore.jpa.AttributeDefEntity;
import com.db.assetstore.service.type.AttributeDefinitionRegistry;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.db.assetstore.schema.TypeSchemaRegistry;

import java.util.Map;

/**
 * Bootstraps attribute definitions into the database at application startup.
 * Converted from CommandLineRunner to a service reacting to ApplicationReadyEvent.
 */
@Service
public class AttributeDefinitionsBootstrapService {
    private static final Logger log = LoggerFactory.getLogger(AttributeDefinitionsBootstrapService.class);

    private final EntityManager em;

    public AttributeDefinitionsBootstrapService(EntityManager em) {
        this.em = em;
    }


    @Transactional
    public void bootstrap() {
        var reg = AttributeDefinitionRegistry.getInstance();
        var types = TypeSchemaRegistry.getInstance().supportedTypes();
        for (AssetType t : types) {
            Map<String, AttributeDefinitionRegistry.Def> defs = reg.getDefinitions(t);
            for (var entry : defs.entrySet()) {
                String name = entry.getKey();
                var def = entry.getValue();
                Long count = em.createQuery(
                                "SELECT COUNT(d) FROM AttributeDefEntity d WHERE d.type = :type AND d.name = :name",
                                Long.class)
                        .setParameter("type", t)
                        .setParameter("name", name)
                        .getSingleResult();
                if (count == null || count == 0L) {
                    AttributeDefEntity e = new AttributeDefEntity(t, def.name(), def.valueType().dbName(), def.required());
                    em.persist(e);
                }
            }
        }
        log.info("Bootstrapped attribute definitions from JSON Schemas to DB");
    }
}
