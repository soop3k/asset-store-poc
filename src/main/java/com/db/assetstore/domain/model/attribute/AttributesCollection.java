package com.db.assetstore.domain.model.attribute;

import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@RequiredArgsConstructor
public final class AttributesCollection {

    private final LinkedHashMap<String, List<AttributeValue<?>>> data;

    public static AttributesCollection empty() {
        return new AttributesCollection(new LinkedHashMap<>());
    }

    public static AttributesCollection fromFlat(Collection<AttributeValue<?>> flat) {
        if (flat == null || flat.isEmpty()) return empty();
        LinkedHashMap<String, List<AttributeValue<?>>> map = new LinkedHashMap<>();
        for (AttributeValue<?> av : flat) {
            if (av == null) continue;
            map.computeIfAbsent(av.name(), k -> new ArrayList<>()).add(av);
        }
        return new AttributesCollection(map);
    }

    public static AttributesCollection fromMap(Map<String, List<AttributeValue<?>>> map) {
        if (map == null || map.isEmpty()) return empty();
        LinkedHashMap<String, List<AttributeValue<?>>> copy = new LinkedHashMap<>();
        map.forEach((k, v) -> {
            if (k == null || v == null || v.isEmpty()) return;
            copy.put(k, new ArrayList<>(v));
        });
        return new AttributesCollection(copy);
    }

    @JsonCreator
    public static AttributesCollection jsonCreate(Map<String, List<AttributeValue<?>>> json) {
        return fromMap(json);
    }

    @JsonValue
    public Map<String, List<AttributeValue<?>>> json() {
        return asMapView();
    }

    public Map<String, List<AttributeValue<?>>> asMapView() {
        LinkedHashMap<String, List<AttributeValue<?>>> out = new LinkedHashMap<>();
        data.forEach((k, v) -> out.put(k, Collections.unmodifiableList(v)));
        return Collections.unmodifiableMap(out);
    }

    public List<AttributeValue<?>> asListView() {
        ArrayList<AttributeValue<?>> out = new ArrayList<>();
        data.values().forEach(out::addAll);
        return Collections.unmodifiableList(out);
    }

    public boolean isEmpty() { return data.isEmpty(); }
    public int size() { return data.size(); }

    public Optional<AttributeValue<?>> getFirst(String name) {
        var vs = data.get(name);
        return (vs == null || vs.isEmpty()) ? Optional.empty() : Optional.of(vs.get(0));
    }

    /**
     * Backward-compatible convenience alias used by tests.
     * @deprecated Use {@link #getFirst(String)} instead for clearer semantics.
     */
    @Deprecated
    public Optional<AttributeValue<?>> getFirstByName(String name) {
        return getFirst(name);
    }

    /**
     * Gets the first attribute value for the given name and casts it to the specified type.
     * Validates type compatibility before casting to prevent ClassCastException.
     * 
     * @param name the attribute name
     * @param type the expected type class
     * @return Optional containing the typed value, or empty if not found or type incompatible
     * @throws IllegalArgumentException if type is null
     */
    public <T> Optional<T> getOne(String name, Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return getFirst(name)
            .map(AttributeValue::value)
            .filter(value -> value == null || type.isInstance(value))
            .map(type::cast);
    }

    public <T> List<T> getMany(String name, Class<T> type) {
        var vs = data.get(name);
        if (vs == null || vs.isEmpty()) return List.of();
        ArrayList<T> out = new ArrayList<>(vs.size());
        for (var av : vs) {
            Object v = av.value();
            if (v != null) out.add(type.cast(v));
        }
        return Collections.unmodifiableList(out);
    }

    public AttributesCollection set(String name, String v)      { return putOne(new AVString(req(name), v)); }
    public AttributesCollection set(String name, Boolean v)     { return putOne(new AVBoolean(req(name), v)); }
    public AttributesCollection set(String name, BigDecimal v)  { return putOne(new AVDecimal(req(name), v)); }
    public AttributesCollection set(String name, Number v) {
        BigDecimal bd = (v == null) ? null : new BigDecimal(v.toString());
        return putOne(new AVDecimal(req(name), bd));
    }

    public AttributesCollection add(String name, String v)      { return append(new AVString(req(name), v)); }
    public AttributesCollection add(String name, Boolean v)     { return append(new AVBoolean(req(name), v)); }
    public AttributesCollection add(String name, BigDecimal v)  { return append(new AVDecimal(req(name), v)); }
    public AttributesCollection add(String name, Number v) {
        BigDecimal bd = (v == null) ? null : new BigDecimal(v.toString());
        return append(new AVDecimal(req(name), bd));
    }
    public AttributesCollection add(AttributeValue<?> av)       { return append(av); }

    public AttributesCollection clear(String name, AttributeType type) {
        return switch (Objects.requireNonNull(type)) {
            case STRING  -> set(name, (String) null);
            case DECIMAL -> set(name, (BigDecimal) null);
            case BOOLEAN -> set(name, (Boolean) null);
        };
    }

    private AttributesCollection putOne(AttributeValue<?> av) {
        LinkedHashMap<String, List<AttributeValue<?>>> copy = copyData();
        copy.put(av.name(), new ArrayList<>(List.of(av)));
        return new AttributesCollection(copy);
    }

    private AttributesCollection append(AttributeValue<?> av) {
        if (av == null) return this;
        LinkedHashMap<String, List<AttributeValue<?>>> copy = copyData();
        copy.computeIfAbsent(av.name(), k -> new ArrayList<>()).add(av);
        return new AttributesCollection(copy);
    }

    private LinkedHashMap<String, List<AttributeValue<?>>> copyData() {
        LinkedHashMap<String, List<AttributeValue<?>>> copy = new LinkedHashMap<>(data.size());
        data.forEach((k, v) -> copy.put(k, new ArrayList<>(v)));
        return copy;
    }

    private static String req(String n) { return Objects.requireNonNull(n, "name"); }
}
