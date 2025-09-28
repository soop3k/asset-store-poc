package com.db.assetstore.infra.service.search;

import com.db.assetstore.domain.search.Condition;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.infra.jpa.search.AttributePredicateVisitor;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class AssetSearchSpecificationService {

    public <T> Specification<T> buildSpec(SearchCriteria criteria) {
        return (root, query, cb) -> {
            var preds = new ArrayList<Predicate>();
            preds.add(cb.equal(root.get("deleted"), 0));

            if (criteria != null) {
                if (criteria.type() != null) {
                    preds.add(cb.equal(root.get("type"), criteria.type()));
                }
                var conditions = criteria.conditions();
                if (!conditions.isEmpty()) {
                    for (var c : conditions) {
                        preds.add(attributeMatch(cb, root, c));
                    }
                }
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private <T> Predicate attributeMatch(CriteriaBuilder cb,
                                         Root<T> root,
                                         Condition<?> cond) {
        Join<Object, Object> a = root.join("attributes", JoinType.INNER);
        List<Predicate> sp = new ArrayList<>();
        sp.add(cb.equal(a.get("name"), cond.attribute()));
        sp.add(AttributePredicateVisitor.build(cb, a, cond));
        return cb.and(sp.toArray(new Predicate[0]));
    }
}
