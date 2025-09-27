package com.db.assetstore.domain.model.attribute;

import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public final class AttributesCollection {

    private final LinkedHashMap<String, List<AttributeValue<?>>> data;

    public static AttributesCollection empty() {
        return new AttributesCollection(new LinkedHashMap<>());
    }

    public static AttributesCollection fromFlat(@NonNull Collection<AttributeValue<?>> flat) {
        if (flat.isEmpty()) {
            return empty();
        }
        var map = new LinkedHashMap<String, List<AttributeValue<?>>>();
        for (var av : flat) {
            Objects.requireNonNull(av, "attribute value must not be null");
            map.computeIfAbsent(av.name(), k -> new ArrayList<>()).add(av);
        }
        return new AttributesCollection(map);
    }

    public static AttributesCollection fromMap(@NonNull Map<String, List<AttributeValue<?>>> map) {
        if (map.isEmpty()) {
            return empty();
        }
        var copy = new LinkedHashMap<String, List<AttributeValue<?>>>();
        map.forEach((k, v) -> {
            Objects.requireNonNull(k, "attribute name must not be null");
            Objects.requireNonNull(v, "attribute values must not be null");
            if (!v.isEmpty()) {
                copy.put(k, new ArrayList<>(v));
            }
        });
        return new AttributesCollection(copy);
    }


    public Map<String, List<AttributeValue<?>>> asMap() {
        LinkedHashMap<String, List<AttributeValue<?>>> out = new LinkedHashMap<>();
        data.forEach(
                (k, v) -> out.put(k, Collections.unmodifiableList(v)));
        return Collections.unmodifiableMap(out);
    }

    public List<AttributeValue<?>> asList() {
        ArrayList<AttributeValue<?>> out = new ArrayList<>();
        data.values().forEach(out::addAll);
        return Collections.unmodifiableList(out);
    }

    public boolean isEmpty() { return data.isEmpty(); }
    public int size() { return data.size(); }

    public Optional<AttributeValue<?>> get(String name) {
        var vs = data.get(name);
        return (vs == null || vs.isEmpty()) ? Optional.empty() : Optional.of(vs.get(0));
    }

    public Optional<AttributeValue<?>> get(String name, int index) {
        var vs = data.get(name);
        return (vs == null || vs.isEmpty()) ? Optional.empty() : Optional.of(vs.get(index));
    }

    public List<AttributeValue<?>> getAll(String name) {
        var vs = data.get(name);
        if (vs == null || vs.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(vs);
    }

    public <T> Optional<T> get(String name, Class<T> type) {
        return get(name).map(AttributeValue::value).map(type::cast);
    }

    public <T> List<T> getMany(String name, Class<T> type) {
        var vs = data.get(name);
        if (vs == null || vs.isEmpty()) {
            return List.of();
        }
        var out = new ArrayList<T>(vs.size());
        for (var av : vs) {
            Object v = av.value();
            if (v != null) {
                out.add(type.cast(v));
            }
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

    public AttributesCollection clear(String name, @NonNull AttributeType type) {
        return switch (type) {
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

    private AttributesCollection append(@NonNull AttributeValue<?> av) {
        LinkedHashMap<String, List<AttributeValue<?>>> copy = copyData();
        copy.computeIfAbsent(av.name(), k -> new ArrayList<>()).add(av);
        return new AttributesCollection(copy);
    }

    private LinkedHashMap<String, List<AttributeValue<?>>> copyData() {
        LinkedHashMap<String, List<AttributeValue<?>>> copy = new LinkedHashMap<>(data.size());
        data.forEach((k, v) -> copy.put(k, new ArrayList<>(v)));
        return copy;
    }

    private static String req(@NonNull String n) { return n; }
}
