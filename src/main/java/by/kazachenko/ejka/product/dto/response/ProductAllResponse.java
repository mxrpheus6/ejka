package by.kazachenko.ejka.product.dto.response;

import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductCategory;
import by.kazachenko.ejka.product.model.enums.ProductRating;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductAllResponse(
        UUID id,
        String barcode,
        String title,
        ProductCategory category,
        List<ProductImageResponse> images,
        ProductRating rating,
        BigDecimal userRating,
        Integer reviewsCount,
        ModerationStatus moderationStatus,
        Integer calories,
        BigDecimal proteins,
        BigDecimal fats,
        BigDecimal carbohydrates
) {

}
