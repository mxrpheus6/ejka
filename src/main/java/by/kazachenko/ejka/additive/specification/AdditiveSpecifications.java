package by.kazachenko.ejka.additive.specification;

import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.additive.model.Origin;
import by.kazachenko.ejka.additive.model.enums.DangerLevel;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class AdditiveSpecifications {

    public static Specification<Additive> hasCategory(String category) {
        return (root, query, cb) ->
                (category == null || category.isBlank()) ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Additive> hasDangerLevel(DangerLevel dangerLevel) {
        return (root, query, cb) ->
                dangerLevel == null ? null : cb.equal(root.get("dangerLevel"), dangerLevel);
    }

    public static Specification<Additive> hasOrigins(List<String> originTypes) {
        return (root, query, cb) -> {
            if (originTypes == null || originTypes.isEmpty()) {
                return null;
            }

            query.distinct(true);
            Join<Additive, Origin> originsJoin = root.join("origins");
            return originsJoin.get("type").in(originTypes);
        };
    }
}