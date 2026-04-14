package by.kazachenko.ejka.product.dto.response;

import by.kazachenko.ejka.additive.dto.response.AdditiveResponse;
import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String barcode,
        String title,
        ProductCategory category,
        List<ProductImageResponse> images,
        Integer nutritionScore,
        ProductScoreResponse scoreDetails,
        BigDecimal userRating,
        Integer reviewsCount,
        Instant createdAt,
        ModerationStatus moderationStatus,
        Integer calories,
        BigDecimal proteins,
        BigDecimal fats,
        BigDecimal carbohydrates,
        Set<AdditiveResponse> additives
) {

}
