package by.kazachenko.ejka.product.specification;

import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductCategory;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecifications {

    public static Specification<Product> hasBarcode(String barcode) {
        return (root, query, cb) ->
                (barcode == null || barcode.isBlank()) ? null : cb.equal(root.get("barcode"), barcode);
    }

    public static Specification<Product> titleSimilarTo(String searchQuery, Double threshold) {
        return (root, query, cb) -> {
            if (searchQuery == null || searchQuery.isBlank()) {
                return null;
            }

            Expression<String> coalesceTitle = cb.coalesce(root.get("title"), "");

            Expression<String> softHyphen = cb.function("chr", String.class, cb.literal(173));

            Expression<String> sanitizedTitle = cb.function(
                    "replace",
                    String.class,
                    coalesceTitle,
                    softHyphen,
                    cb.literal("")
            );

            Expression<Double> similarityFunc = cb.function(
                    "word_similarity",
                    Double.class,
                    cb.literal(searchQuery),
                    sanitizedTitle
            );

            double actualThreshold = threshold != null ? threshold : 0.25;

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.orderBy(cb.desc(similarityFunc));
            }

            return cb.greaterThan(similarityFunc, actualThreshold);
        };
    }

    public static Specification<Product> hasCategory(ProductCategory category) {
        return (root, query, cb) ->
                category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Product> hasModerationStatus(ModerationStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("moderationStatus"), status);
    }

    public static Specification<Product> createdBy(UUID creatorId) {
        return (root, query, cb) ->
                creatorId == null ? null : cb.equal(root.get("creator").get("id"), creatorId);
    }

    public static Specification<Product> caloriesBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("calories"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("calories"), min);
            return cb.lessThanOrEqualTo(root.get("calories"), max);
        };
    }

    public static Specification<Product> minUserRating(BigDecimal minRating) {
        return (root, query, cb) ->
                minRating == null ? null : cb.greaterThanOrEqualTo(root.get("userRating"), minRating);
    }

    public static Specification<Product> containsAdditives(List<UUID> additiveIds) {
        return (root, query, cb) -> {
            if (additiveIds == null || additiveIds.isEmpty()) {
                return null;
            }

            query.distinct(true);
            Join<Product, Additive> additivesJoin = root.join("additives");
            return additivesJoin.get("id").in(additiveIds);
        };
    }

    public static Specification<Product> hasCreatorId(UUID creatorId) {
        return (root, query, cb) ->
                creatorId == null ? null : cb.equal(root.get("creator").get("id"), creatorId);
    }
}
