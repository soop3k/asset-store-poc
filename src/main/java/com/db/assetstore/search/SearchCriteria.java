package com.db.assetstore.search;

import com.db.assetstore.AssetType;

import java.util.ArrayList;
import java.util.List;

public final class SearchCriteria {
    private final AssetType type; // optional
    private final List<Condition> conditions;

    private SearchCriteria(AssetType type, List<Condition> conditions) {
        this.type = type;
        this.conditions = List.copyOf(conditions);
    }

    public AssetType type() { return type; }
    public List<Condition> conditions() { return conditions; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private AssetType type;
        private final List<Condition> conditions = new ArrayList<>();

        public Builder type(AssetType type) {
            this.type = type;
            return this;
        }

        public Builder where(String name, Operator op, Object value) {
            conditions.add(new Condition(name, op, value));
            return this;
        }

        public SearchCriteria build() {
            return new SearchCriteria(type, conditions);
        }
    }
}
