package com.db.assetstore.domain.service;

import lombok.extern.slf4j.Slf4j;
import com.db.assetstore.domain.json.AssetJsonFactory;
import com.db.assetstore.domain.json.AssetAttributeConverter;
import com.db.assetstore.domain.service.validation.AssetAttributeValidationService;
import com.db.assetstore.AssetType;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AttributeHistory;
import com.db.assetstore.domain.model.AttributeValue;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import com.db.assetstore.infra.repository.AttributeHistoryRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.domain.search.Condition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.*;
import java.time.Instant;
import java.util.*;

@Slf4j
public class AssetService {
    private final AssetRepository assetRepo;
    private final AttributeRepository attributeRepo;
    private final AttributeDefRepository defRepo;
    private final AttributeHistoryRepository historyRepo;
    private final AssetJsonFactory assetJsonFactory = new AssetJsonFactory();
    private final AssetAttributeValidationService validationService = new AssetAttributeValidationService();
    private final ObjectMapper mapper = JsonMapperProvider.get();
    private final AssetAttributeConverter attrConverter = new AssetAttributeConverter();

    public AssetService(AssetRepository assetRepo,
                        AttributeRepository attributeRepo,
                        AttributeDefRepository defRepo,
                        AttributeHistoryRepository historyRepo) {
        this.assetRepo = Objects.requireNonNull(assetRepo);
        this.attributeRepo = Objects.requireNonNull(attributeRepo);
        this.defRepo = Objects.requireNonNull(defRepo);
        this.historyRepo = Objects.requireNonNull(historyRepo);
    }

    @Transactional
    public String addAsset(Asset asset) {
        log.info("Adding asset: type={}, id={}", asset.getType(), asset.getId());
        AssetEntity e = AssetMapper.INSTANCE.toEntity(asset);
        insertAllOnCreate(e, asset.getAttributesByName().values());
        return e.getId();
    }

    @Transactional
    public String addAssetFromJson(String json) {
        Objects.requireNonNull(json, "json");
        validationService.validateEnvelope(json);
        Asset asset = assetJsonFactory.fromJson(json);
        return addAsset(asset);
    }

    @Transactional
    public String addAssetFromJson(AssetType type, String json) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(json, "json");
        validationService.validateForType(type, json);
        Asset asset = assetJsonFactory.fromJsonForType(type, json);
        return addAsset(asset);
    }

    @Transactional
    public List<String> addAssetsFromJsonArray(String jsonArray) {
        Objects.requireNonNull(jsonArray, "jsonArray");
        try {
            JsonNode node = mapper.readTree(jsonArray);
            if (node == null || !node.isArray()) {
                throw new IllegalArgumentException("Expected JSON array of assets");
            }
            List<String> ids = new ArrayList<>();
            for (JsonNode item : node) {
                validationService.validateEnvelope(item);
                Asset asset = assetJsonFactory.fromJson(item);
                String id = addAsset(asset);
                ids.add(id);
            }
            return ids;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON array payload: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void removeAsset(String assetId) {
        assetRepo.findById(assetId).ifPresent(e -> {
            e.setDeleted(1);
            assetRepo.save(e);
        });
    }

    @Transactional(readOnly = true)
    public Optional<Asset> getAsset(String assetId) {
        return assetRepo.findByIdAndDeleted(assetId, 0).map(e -> {
            Map<String, AttributeValue<?>> attrs = new LinkedHashMap<>();
            for (AttributeEntity r : e.getAttributes()) {
                if (r == null) continue;
                AttributeValue<?> av = AttributeMapper.INSTANCE.toModel(r);
                attrs.put(av.name(), av);
            }
            Asset a = Asset.builder()
                    .id(e.getId())
                    .type(e.getType())
                    .createdAt(e.getCreatedAt())
                    .attrs(attrs.values())
                    .build();
            AssetMapper.INSTANCE.updateModelFromEntity(e, a);
            return a;
        });
    }

    @Transactional(readOnly = true)
    public List<Asset> search(SearchCriteria criteria) {
        List<AssetEntity> entities = searchEntities(criteria);
        List<Asset> result = new ArrayList<>(entities.size());
        for (AssetEntity e : entities) {
            Map<String, AttributeValue<?>> attrs = new LinkedHashMap<>();
            for (AttributeEntity r : e.getAttributes()) {
                if (r == null) continue;
                AttributeValue<?> av = AttributeMapper.INSTANCE.toModel(r);
                attrs.put(av.name(), av);
            }
            Asset a = Asset.builder()
                    .id(e.getId())
                    .type(e.getType())
                    .createdAt(e.getCreatedAt())
                    .attrs(attrs.values())
                    .build();
            AssetMapper.INSTANCE.updateModelFromEntity(e, a);
            result.add(a);
        }
        return result;
    }

    public List<AttributeHistory> history(String assetId) {
        List<AttributeHistory> out = new ArrayList<>();
        var rows = historyRepo.findAllByAsset_IdOrderByChangedAt(assetId);
        for (var h : rows) {
            out.add(new AttributeHistory(
                    h.getAsset() != null ? h.getAsset().getId() : assetId,
                    h.getName(),
                    h.getValue(),
                    h.getValueType(),
                    h.getChangedAt()
            ));
        }
        return out;
    }

    @Transactional
    public void updateAssetFromJson(String assetId, String json) {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(json, "json");
        final AssetEntity e = assetRepo.findByIdAndDeleted(assetId, 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + assetId));
        try {
            JsonNode node = mapper.readTree(json);
            if (node == null || !node.isObject()) {
                throw new IllegalArgumentException("Invalid JSON payload for asset update: expected object");
            }
            final String originalId = e.getId();
            final var originalType = e.getType();

            JsonNode attrsNode = ((ObjectNode) node).get("attributes");
            List<AttributeValue<?>> incomingAttrs = Collections.emptyList();
            if (attrsNode != null) {
                incomingAttrs = new AssetAttributeConverter().readAttributes(e.getType(), attrsNode);
            }

            mapper.readerForUpdating(e).readValue(node);
            e.setId(originalId);
            e.setType(originalType);
            assetRepo.save(e);

            upsertOnly(e, incomingAttrs);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to apply asset update: " + ex.getMessage(), ex);
        }
    }


    private Map<String, AttributeDefEntity> resolveAttrDefs(AssetType type) {
        if (type == null) return Collections.emptyMap();
        boolean hasSchema = TypeSchemaRegistry.getInstance().getSchemaPath(type).isPresent();
        if (hasSchema) {
            Map<String, AttributeDefinitionRegistry.Def> regDefs = AttributeDefinitionRegistry.getInstance().getDefinitions(type);
            Map<String, AttributeDefEntity> tmp = new HashMap<>();
            for (var entry : regDefs.entrySet()) {
                var d = entry.getValue();
                tmp.put(entry.getKey(), new AttributeDefEntity(type, d.name(), d.valueType().dbName(), d.required()));
            }
            return tmp;
        }
        // fall back to DB-stored definitions when no schema
        List<AttributeDefEntity> defs = defRepo.findAllByType(type);
        Map<String, AttributeDefEntity> map = new HashMap<>();
        for (AttributeDefEntity d : defs) map.put(d.getName(), d);
        return map;
    }

    private static void ensureDefinedFull(Map<String, AttributeDefEntity> defs, String name) {
        if (defs == null || !defs.containsKey(name)) {
            throw new IllegalArgumentException("Attribute not defined for asset type: " + name);
        }
    }

    private void upsertOnly(AssetEntity asset, Collection<AttributeValue<?>> attributes) {
        Objects.requireNonNull(asset, "asset entity");
        Objects.requireNonNull(attributes, "attributes");
        if (attributes.isEmpty()) return;
        Map<String, AttributeEntity> existing = new HashMap<>();
        for (AttributeEntity a : asset.getAttributes()) {
            if (a != null) existing.put(a.getName(), a);
        }
        Instant now = Instant.now();
        for (AttributeValue<?> av : attributes) {
            if (av == null) continue;
            AttributeEntity cur = existing.get(av.name());
            if (cur == null) {
                AttributeEntity created = AttributeMapper.INSTANCE.toEntity(asset, av);
                asset.getAttributes().add(created);
                attributeRepo.save(created);
                existing.put(created.getName(), created);
            } else {
                attributeRepo.save(cur);
            }
        }
    }

    private void insertAllOnCreate(AssetEntity asset, Collection<AttributeValue<?>> attributes) {
        Instant now = Instant.now();
        for (AttributeValue<?> av : attributes) {
            if (av == null) continue;
            AttributeEntity e = AttributeMapper.INSTANCE.toEntity(asset, av);
            asset.getAttributes().add(e);
        }
        assetRepo.save(asset);
    }

    // search support (Specification builder) copied from previous implementation
    private List<AssetEntity> searchEntities(SearchCriteria criteria) {
        return assetRepo.findAll(buildSpec(criteria));
    }

    private Specification<AssetEntity> buildSpec(SearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), 0));
            if (criteria != null) {
                if (criteria.type() != null) {
                    predicates.add(cb.equal(root.get("type"), criteria.type()));
                }
                if (criteria.conditions() != null && !criteria.conditions().isEmpty()) {
                    Join<AssetEntity, AttributeEntity> join = root.join("attributes", JoinType.INNER);
                    for (Condition c : criteria.conditions()) {
                        Predicate nameEq = cb.equal(join.get("name"), c.attribute());
                        Object val = c.value();
                        var op = c.operator();
                        Predicate valuePred;
                        switch (op) {
                            case EQ:
                                if (val instanceof Boolean) {
                                    valuePred = cb.equal(join.get("valueBool"), (Boolean) val);
                                } else if (val instanceof Number || val instanceof java.math.BigDecimal) {
                                    java.math.BigDecimal bd = asBigDecimal(val);
                                    valuePred = bd == null ? cb.isNull(join.get("valueNum")) : cb.equal(join.get("valueNum"), bd);
                                } else {
                                    valuePred = cb.equal(join.get("valueStr"), AttributeMapper.INSTANCE.stringify(val));
                                }
                                break;
                            case LIKE:
                                valuePred = cb.like(join.get("valueStr"), AttributeMapper.INSTANCE.stringify(val));
                                break;
                            case GT: {
                                java.math.BigDecimal bd = asBigDecimal(val);
                                valuePred = bd == null ? cb.conjunction() : cb.greaterThan(join.get("valueNum"), bd);
                                break; }
                            case LT: {
                                java.math.BigDecimal bd = asBigDecimal(val);
                                valuePred = bd == null ? cb.conjunction() : cb.lessThan(join.get("valueNum"), bd);
                                break; }
                            default:
                                valuePred = cb.conjunction();
                        }
                        predicates.add(cb.and(nameEq, valuePred));
                    }
                }
            }
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    private static java.math.BigDecimal asBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof java.math.BigDecimal bd) return bd;
        if (o instanceof Number n) return new java.math.BigDecimal(n.toString());
        try { return new java.math.BigDecimal(o.toString()); } catch (Exception e) { return null; }
    }
}
