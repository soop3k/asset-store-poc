package com.db.assetstore.repo;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import com.db.assetstore.jpa.AssetEntity;
import com.db.assetstore.jpa.AttributeEntity;
import com.db.assetstore.jpa.AttributeDefEntity;
import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeHistory;
import com.db.assetstore.model.AttributeValue;
import com.db.assetstore.mapper.AssetMapper;
import com.db.assetstore.search.Condition;
import com.db.assetstore.search.Operator;
import com.db.assetstore.search.SearchCriteria;

import java.time.Instant;
import java.util.*;
import com.db.assetstore.AssetType;

@Slf4j
public class JpaAssetRepository implements AssetRepository {
    private final EntityManager em;

    public JpaAssetRepository(EntityManager em) {
        this.em = Objects.requireNonNull(em);
    }

    @Override
    public String saveAsset(Asset asset) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            log.info("Saving asset: type={}, id={}", asset.getType(), asset.getId());
            AssetEntity e = AssetMapper.INSTANCE.toEntity(asset);
            em.persist(e);
            // Validate attributes against definitions for this type
            Map<String, AttributeDefEntity> defs = loadAttrDefsFull(e.getType());
            Set<String> provided = new HashSet<>();
            if (asset.attributes() != null && !asset.attributes().isEmpty()) {
                Instant now = Instant.now();
                for (AttributeValue<?> av : asset.attributes().values()) {
                    ensureDefinedFull(defs, av.name());
                    provided.add(av.name());
                    // History note: AttributeEntity constructor appends initial history entry
                    AttributeEntity cur = com.db.assetstore.mapper.AttributeMapper.toEntity(e, av, now);
                    // Keep the bi-directional association in sync for the current persistence context
                    e.getAttributes().add(cur);
                    em.persist(cur);
                }
            }
            // ensure all required attributes are provided
            for (AttributeDefEntity def : defs.values()) {
                if (Boolean.TRUE.equals(def.isRequired()) && !provided.contains(def.getName())) {
                    throw new IllegalArgumentException("Missing required attribute: " + def.getName());
                }
            }
            tx.commit();
            return asset.getId();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    @Override
    public void softDelete(String assetId) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            AssetEntity e = em.find(AssetEntity.class, assetId, LockModeType.NONE);
            if (e != null) {
                e.setDeleted(1);
                em.merge(e);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    @Override
    public Optional<Asset> findById(String assetId) {
        AssetEntity e = em.find(AssetEntity.class, assetId);
        if (e == null || e.getDeleted() != 0) {
            return Optional.empty();
        }
        Map<String, AttributeValue<?>> attrs = loadAttributes(e.getId());
        Asset a = new Asset(e.getId(), e.getType(), e.getCreatedAt(), attrs.values());
        AssetMapper.INSTANCE.updateModelFromEntity(e, a);
        return Optional.of(a);
    }

    @Override
    public List<Asset> search(SearchCriteria criteria) {
        // Delegate entity fetching to a shared implementation and just map to the domain model here
        List<AssetEntity> entities = searchEntities(criteria);
        List<Asset> result = new ArrayList<>(entities.size());
        for (AssetEntity e : entities) {
            Map<String, AttributeValue<?>> attrs = loadAttributes(e.getId());
            Asset a = new Asset(e.getId(), e.getType(), e.getCreatedAt(), attrs.values());
            AssetMapper.INSTANCE.updateModelFromEntity(e, a);
            result.add(a);
        }
        return result;
    }

    @Override
    public List<AttributeHistory> history(String assetId) {
        // Use redundant columns on history entity to avoid relying on lazy attribute join
        TypedQuery<Object[]> q = em.createQuery(
                "SELECT h.asset.id, h.name, h.value, h.valueType, h.changedAt FROM AttributeHistoryEntity h WHERE h.asset.id = :id ORDER BY h.changedAt",
                Object[].class);
        q.setParameter("id", assetId);
        List<Object[]> rows = q.getResultList();
        List<AttributeHistory> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            out.add(new AttributeHistory((String) r[0], (String) r[1], (String) r[2], (String) r[3], (java.time.Instant) r[4]));
        }
        return out;
    }

    @Override
    public void setAttributes(String assetId, Collection<AttributeValue<?>> attributes) {
        throw new UnsupportedOperationException("setAttributes not implemented in this repository version");
    }

    private AttributeEntity findAttribute(String assetId, String name) {
        List<AttributeEntity> found = em.createQuery(
                        "SELECT a FROM AttributeEntity a WHERE a.asset.id = :id AND a.name = :name",
                        AttributeEntity.class)
                .setParameter("id", assetId)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList();
        return found.isEmpty() ? null : found.get(0);
    }

    private Map<String, AttributeValue<?>> loadAttributes(String assetId) {
        List<AttributeEntity> rows = em.createQuery(
                        "SELECT a FROM AttributeEntity a WHERE a.asset.id = :id",
                        AttributeEntity.class)
                .setParameter("id", assetId)
                .getResultList();
        Map<String, AttributeValue<?>> map = new LinkedHashMap<>();
        for (AttributeEntity r : rows) {
            AttributeValue<?> av = com.db.assetstore.mapper.AttributeMapper.toModel(r);
            map.put(av.name(), av);
        }
        return map;
    }


    private static Double asDouble(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        try { return Double.valueOf(o.toString()); } catch (Exception e) { return null; }
    }


    private Asset toModel(AssetEntity e) {
        Map<String, AttributeValue<?>> attrs = loadAttributes(e.getId());
        Asset a = new Asset(e.getId(), e.getType(), e.getCreatedAt(), attrs.values());
        a.setVersion(e.getVersion());
        a.setStatus(e.getStatus());
        a.setSubtype(e.getSubtype());
        a.setStatusEffectiveTime(e.getStatusEffectiveTime());
        a.setCreatedBy(e.getCreatedBy());
        a.setModifiedAt(e.getModifiedAt());
        a.setModifiedBy(e.getModifiedBy());
        a.setSoftDelete(e.getDeleted() != 0);
        a.setNotionalAmount(e.getNotionalAmount());
        a.setYear(e.getYear());
        a.setWh(e.getWh());
        a.setSourceSystemName(e.getSourceSystemName());
        a.setExternalReference(e.getExternalReference());
        a.setDescription(e.getDescription());
        a.setCurrency(e.getCurrency());
        return a;
    }

    // --- compact helpers ---
    private Map<String, String> loadAttrDefs(AssetType type) {
        if (type == null) {
            return Collections.emptyMap();
        }
        List<AttributeDefEntity> defs = em.createQuery(
                "SELECT d FROM AttributeDefEntity d WHERE d.type = :type", AttributeDefEntity.class)
                .setParameter("type", type)
                .getResultList();
        Map<String, String> map = new HashMap<>();
        for (AttributeDefEntity d : defs) map.put(d.getName(), d.getValueType());
        return map;
    }

    private Map<String, AttributeDefEntity> loadAttrDefsFull(AssetType type) {
        if (type == null) {
            return Collections.emptyMap();
        }
        List<AttributeDefEntity> defs = em.createQuery(
                "SELECT d FROM AttributeDefEntity d WHERE d.type = :type", AttributeDefEntity.class)
                .setParameter("type", type)
                .getResultList();
        Map<String, AttributeDefEntity> map = new HashMap<>();
        for (AttributeDefEntity d : defs) map.put(d.getName(), d);
        return map;
    }

    private static void ensureDefined(Map<String, String> defs, String name) {
        if (defs == null || !defs.containsKey(name)) {
            throw new IllegalArgumentException("Attribute not defined for asset type: " + name);
        }
    }

    private static void ensureDefinedFull(Map<String, AttributeDefEntity> defs, String name) {
        if (defs == null || !defs.containsKey(name)) {
            throw new IllegalArgumentException("Attribute not defined for asset type: " + name);
        }
    }

    private Predicate attrPredicate(Condition c, Join<AssetEntity, AttributeEntity> join, CriteriaBuilder cb) {
        Predicate nameEq = cb.equal(join.get("name"), c.attribute());
        Object val = c.value();
        Operator op = c.operator();

        // Build predicate based on operator; string comparisons operate on stored string values
        Predicate valuePred = switch (op) {
            case EQ -> cb.equal(join.get("value"), com.db.assetstore.mapper.AttributeMapper.stringify(val));
            case LIKE -> cb.like(join.get("value"), com.db.assetstore.mapper.AttributeMapper.stringify(val));
            case GT -> cb.gt(join.get("value").as(Double.class), asDouble(val));
            case LT -> cb.lt(join.get("value").as(Double.class), asDouble(val));
            default -> cb.conjunction();
        };
        return cb.and(nameEq, valuePred);
    }

    // Build predicates for asset search in one place to keep logic consistent and clear
    private List<Predicate> buildPredicates(SearchCriteria criteria, Root<AssetEntity> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        // Always exclude soft-deleted assets
        predicates.add(cb.equal(root.get("deleted"), 0));
        if (criteria != null) {
            if (criteria.type() != null) {
                predicates.add(cb.equal(root.get("type"), criteria.type()));
            }
            if (criteria.conditions() != null && !criteria.conditions().isEmpty()) {
                Join<AssetEntity, AttributeEntity> join = root.join("attributes", JoinType.INNER);
                for (Condition c : criteria.conditions()) {
                    predicates.add(attrPredicate(c, join, cb));
                }
            }
        }
        return predicates;
    }


    // --- Optional entity-facing accessors for advanced use cases ---
    public Optional<AssetEntity> findEntityById(String assetId) {
        AssetEntity e = em.find(AssetEntity.class, assetId);
        if (e == null || e.getDeleted() != 0) {
            return Optional.empty();
        }
        return Optional.of(e);
    }

    public List<AssetEntity> searchEntities(SearchCriteria criteria) {
        // Build a clear, readable query using helper methods and return the results
        CriteriaQuery<AssetEntity> query = buildSearchEntitiesQuery(criteria);
        return em.createQuery(query).getResultList();
    }

    private CriteriaQuery<AssetEntity> buildSearchEntitiesQuery(SearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AssetEntity> cq = cb.createQuery(AssetEntity.class);
        Root<AssetEntity> root = cq.from(AssetEntity.class);
        List<Predicate> predicates = buildPredicates(criteria, root, cb);
        cq.select(root)
          .where(cb.and(predicates.toArray(new Predicate[0])))
          .distinct(true);
        return cq;
    }
}
