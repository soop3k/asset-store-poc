package com.db.assetstore.infra.jpa;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDate;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.search.Condition;
import com.db.assetstore.domain.search.Operator;
import com.db.assetstore.infra.jpa.search.AttributePredicateVisitor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AttributePredicateVisitorDataTest {

    @Autowired
    EntityManager entityManager;

    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");

    @BeforeEach
    void clearPersistenceContext() {
        entityManager.clear();
    }

    @Test
    void matchesStringValuesCaseInsensitive() {
        AssetEntity blue = newAsset("asset-blue");
        blue.getAttributes().add(new AttributeEntity(blue, "COLOR", "Blue", NOW));

        AssetEntity red = newAsset("asset-red");
        red.getAttributes().add(new AttributeEntity(red, "COLOR", "Red", NOW));

        entityManager.persist(blue);
        entityManager.persist(red);
        entityManager.flush();

        Condition<String> condition = new Condition<>("COLOR", Operator.EQ, AVString.of("COLOR", "blue"));

        List<AssetEntity> found = executeAttributeSearch(condition);

        assertThat(found).extracting(AssetEntity::getId).containsExactly("asset-blue");
    }

    @Test
    void matchesStringLikeUsingWildcardWrapping() {
        AssetEntity blue = newAsset("asset-blue-2");
        blue.getAttributes().add(new AttributeEntity(blue, "COLOR", "Cerulean", NOW));
        entityManager.persist(blue);

        Condition<String> condition = new Condition<>("COLOR", Operator.LIKE, AVString.of("COLOR", "rul"));

        List<AssetEntity> found = executeAttributeSearch(condition);

        assertThat(found).extracting(AssetEntity::getId).containsExactly("asset-blue-2");
    }

    @Test
    void matchesDecimalWithGreaterThanAndLessThan() {
        AssetEntity expensive = newAsset("asset-expensive");
        expensive.getAttributes().add(new AttributeEntity(expensive, "AMOUNT", new BigDecimal("150.00"), NOW));
        entityManager.persist(expensive);

        AssetEntity cheap = newAsset("asset-cheap");
        cheap.getAttributes().add(new AttributeEntity(cheap, "AMOUNT", new BigDecimal("75.00"), NOW));
        entityManager.persist(cheap);

        Condition<BigDecimal> gtCondition = new Condition<>("AMOUNT", Operator.GT, AVDecimal.of("AMOUNT", 100));
        List<AssetEntity> greater = executeAttributeSearch(gtCondition);
        assertThat(greater).extracting(AssetEntity::getId).containsExactly("asset-expensive");

        Condition<BigDecimal> ltCondition = new Condition<>("AMOUNT", Operator.LT, AVDecimal.of("AMOUNT", 100));
        List<AssetEntity> lower = executeAttributeSearch(ltCondition);
        assertThat(lower).extracting(AssetEntity::getId).containsExactly("asset-cheap");
    }

    @Test
    void matchesBooleanEquality() {
        AssetEntity audited = newAsset("asset-audited");
        audited.getAttributes().add(new AttributeEntity(audited, "AUDITED", Boolean.TRUE, NOW));
        entityManager.persist(audited);

        AssetEntity unaudited = newAsset("asset-unaudited");
        unaudited.getAttributes().add(new AttributeEntity(unaudited, "AUDITED", Boolean.FALSE, NOW));
        entityManager.persist(unaudited);

        Condition<Boolean> condition = new Condition<>("AUDITED", Operator.EQ, AVBoolean.of("AUDITED", Boolean.TRUE));

        List<AssetEntity> found = executeAttributeSearch(condition);

        assertThat(found).extracting(AssetEntity::getId).containsExactly("asset-audited");
    }

    @Test
    void matchesDateComparisons() {
        Instant today = Instant.parse("2024-02-15T00:00:00Z");
        Instant yesterday = Instant.parse("2024-02-14T00:00:00Z");
        Instant tomorrow = Instant.parse("2024-02-16T00:00:00Z");

        AssetEntity assetToday = newAsset("asset-today");
        assetToday.getAttributes().add(new AttributeEntity(assetToday, "UPDATED_AT", today, NOW));
        entityManager.persist(assetToday);

        AssetEntity assetYesterday = newAsset("asset-yesterday");
        assetYesterday.getAttributes().add(new AttributeEntity(assetYesterday, "UPDATED_AT", yesterday, NOW));
        entityManager.persist(assetYesterday);

        AssetEntity assetTomorrow = newAsset("asset-tomorrow");
        assetTomorrow.getAttributes().add(new AttributeEntity(assetTomorrow, "UPDATED_AT", tomorrow, NOW));
        entityManager.persist(assetTomorrow);

        Condition<Instant> eqCondition = new Condition<>("UPDATED_AT", Operator.EQ, AVDate.of("UPDATED_AT", today));
        List<AssetEntity> eq = executeAttributeSearch(eqCondition);
        assertThat(eq).extracting(AssetEntity::getId).containsExactly("asset-today");

        Condition<Instant> gtCondition = new Condition<>("UPDATED_AT", Operator.GT, AVDate.of("UPDATED_AT", today));
        List<AssetEntity> gt = executeAttributeSearch(gtCondition);
        assertThat(gt).extracting(AssetEntity::getId).containsExactly("asset-tomorrow");

        Condition<Instant> ltCondition = new Condition<>("UPDATED_AT", Operator.LT, AVDate.of("UPDATED_AT", today));
        List<AssetEntity> lt = executeAttributeSearch(ltCondition);
        assertThat(lt).extracting(AssetEntity::getId).containsExactly("asset-yesterday");
    }

    private AssetEntity newAsset(String id) {
        return AssetEntity.builder()
                .id(id)
                .type(AssetType.CRE)
                .createdAt(NOW)
                .createdBy("tester")
                .build();
    }

    private <T> List<AssetEntity> executeAttributeSearch(Condition<T> condition) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(AssetEntity.class);
        Root<AssetEntity> root = query.from(AssetEntity.class);
        Join<AssetEntity, AttributeEntity> attr = root.join("attributes", JoinType.INNER);

        query.distinct(true);
        query.select(root).where(cb.and(
                cb.equal(attr.get("name"), condition.attribute()),
                AttributePredicateVisitor.build(cb, attr, condition)
        ));

        return entityManager.createQuery(query).getResultList();
    }
}
