package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.AssetLinkEntity;
import org.springframework.data.jpa.domain.Specification;

public final class AssetLinkSpecifications {

    private AssetLinkSpecifications() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Specification<AssetLinkEntity> spec = Specification.where(null);

        public Builder assetId(String assetId) {
            if (assetId != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("assetId"), assetId));
            }
            return this;
        }

        public Builder entityType(String entityType, String entitySubtype) {
            if (entityType != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
            }
            if (entitySubtype != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("entitySubtype"), entitySubtype));
            }
            return this;
        }

        public Builder targetCode(String targetCode) {
            if (targetCode != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("targetCode"), targetCode));
            }
            return this;
        }

        public Builder active(Boolean active) {
            if (active != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
            }
            return this;
        }

        public Specification<AssetLinkEntity> build() {
            return spec;
        }
    }
}
