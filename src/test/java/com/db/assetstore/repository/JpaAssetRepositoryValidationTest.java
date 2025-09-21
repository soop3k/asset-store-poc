package com.db.assetstore.repository;

import com.db.assetstore.AssetType;
import com.db.assetstore.jpa.AttributeDefEntity;
import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies repository-level validations executed in JpaAssetRepository.saveAsset:
 * - Reject undefined attributes for the asset type
 * - Reject save when a required attribute is missing
 * - Accept valid payload when all provided attributes are defined and required ones are present
 */
class JpaAssetRepositoryValidationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private JpaAssetRepository repo;

    @BeforeAll
    static void setupEmf() {
        Properties props = new Properties();
        // In-memory H2; Hibernate will auto-create schema based on entities
        props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        props.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:assetdb;DB_CLOSE_DELAY=-1;MODE=LEGACY");
        props.put("jakarta.persistence.jdbc.user", "sa");
        props.put("jakarta.persistence.jdbc.password", "");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        props.put("hibernate.show_sql", "false");
        emf = Persistence.createEntityManagerFactory("assetPU", props);
    }

    @AfterAll
    static void tearDownEmf() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void openEm() {
        em = emf.createEntityManager();
        repo = new JpaAssetRepository(em);
        seedAttributeDefs();
    }

    @AfterEach
    void closeEm() {
        if (em != null) em.close();
    }

    private void seedAttributeDefs() {
        // Define attributes for CRE type: city (required), area (optional)
        em.getTransaction().begin();
        em.persist(new AttributeDefEntity(AssetType.CRE, "city", "String", true));
        em.persist(new AttributeDefEntity(AssetType.CRE, "area", "Double", false));
        em.getTransaction().commit();
    }

    @Test
    void saveAsset_rejectsMissingRequiredAttribute() {
        // Given: Asset without required 'city'
        Asset asset = new Asset("ID-1", AssetType.CRE, Instant.now(), List.of(
                new AttributeValue<>("area", 120.5, Double.class)
        ));
        // When / Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> repo.saveAsset(asset));
        assertTrue(ex.getMessage().toLowerCase().contains("missing required attribute"));
    }

    @Test
    void saveAsset_rejectsUndefinedAttribute() {
        // Given: Asset with attribute not defined for CRE type
        Asset asset = new Asset("ID-2", AssetType.CRE, Instant.now(), List.of(
                new AttributeValue<>("city", "Warsaw", String.class),
                new AttributeValue<>("floor", 5L, Long.class)
        ));
        // When / Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> repo.saveAsset(asset));
        assertTrue(ex.getMessage().toLowerCase().contains("attribute not defined"));
    }

    @Test
    void saveAsset_acceptsValidAttributes() {
        // Given: Asset with all required and defined attributes
        Asset asset = new Asset("ID-3", AssetType.CRE, Instant.now(), List.of(
                new AttributeValue<>("city", "Krakow", String.class),
                new AttributeValue<>("area", 88.0, Double.class)
        ));
        String id = repo.saveAsset(asset);
        assertEquals("ID-3", id);

        // Verify there are exactly 2 AttributeEntity rows persisted for this asset
        Long count = em.createQuery("SELECT COUNT(a) FROM AttributeEntity a WHERE a.asset.id = :id", Long.class)
                .setParameter("id", "ID-3")
                .getSingleResult();
        assertEquals(2L, count);
    }
}
