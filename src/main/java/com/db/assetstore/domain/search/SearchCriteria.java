package com.db.assetstore.domain.search;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class SearchCriteria {

    private final AssetType type;
    private final List<Condition<?>> conditions;

    public AssetType type() { return type; }
    public List<Condition<?>> conditions() { return conditions; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private AssetType type;
        private final List<Condition<?>> conditions = new ArrayList<>();

        public Builder type(AssetType type) {
            this.type = type;
            return this;
        }

        public <T> Builder where(String name, Operator op, AttributeValue<T> value) {
            conditions.add(new Condition<>(name, op, value));
            return this;
        }

        public SearchCriteria build() {
            return new SearchCriteria(type, conditions);
        }
    }
}
