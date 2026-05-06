package by.kazachenko.ejka.additive.specification;

import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.additive.model.Origin;
import by.kazachenko.ejka.additive.model.enums.DangerLevel;
import jakarta.persistence.criteria.Expression;
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

    public static Specification<Additive> textSimilarTo(String searchQuery, Double threshold) {
        return (root, query, cb) -> {
            if (searchQuery == null || searchQuery.isBlank()) {
                return null;
            }

            String normalizedQuery = searchQuery.trim();
            if (normalizedQuery.matches("\\d+")) {
                normalizedQuery = "E" + normalizedQuery;
            } else if (normalizedQuery.toLowerCase().startsWith("e ") || normalizedQuery.toLowerCase().startsWith("е ")) {
                normalizedQuery = normalizedQuery.replaceFirst("(?i)[eе]\\s+", "E");
            }

            Expression<String> codePath = root.get("code");
            Expression<Boolean> isCodeMatch = cb.like(cb.lower(codePath), "%" + normalizedQuery.toLowerCase() + "%");

            Expression<String> coalesceName = cb.coalesce(root.get("nameRu"), "");

            Expression<Double> similarityFunc = cb.function(
                    "word_similarity",
                    Double.class,
                    cb.literal(normalizedQuery),
                    coalesceName
            );

            double actualThreshold = threshold != null ? threshold : 0.20;

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                Expression<Integer> codeMatchWeight = cb.selectCase()
                        .when(isCodeMatch, 1)
                        .otherwise(0)
                        .as(Integer.class);

                query.orderBy(
                        cb.desc(codeMatchWeight),
                        cb.desc(similarityFunc)
                );
            }

            return cb.or(
                    isCodeMatch,
                    cb.greaterThan(similarityFunc, actualThreshold)
            );
        };
    }
}