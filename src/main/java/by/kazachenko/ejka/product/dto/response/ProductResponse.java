package by.kazachenko.ejka.product.dto.response;

import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductRating;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String barcode,
        String title,
        ProductRating rating,
        Instant createdAt,
        ModerationStatus moderationStatus,
        Integer calories,
        BigDecimal proteins,
        BigDecimal fats,
        BigDecimal carbohydrates
) {

}
