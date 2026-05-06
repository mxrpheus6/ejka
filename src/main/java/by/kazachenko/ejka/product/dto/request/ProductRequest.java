package by.kazachenko.ejka.product.dto.request;

import by.kazachenko.ejka.additive.model.enums.AllergenCategory;
import by.kazachenko.ejka.product.model.enums.ProductCategory;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import java.util.List;
import java.util.Set;
import lombok.Builder;

@Builder
public record ProductRequest(

        @NotBlank(message = "{product_request.barcode.not_blank}")
        String barcode,

        @NotBlank(message = "{product_request.title.not_blank}")
        @Size(min = 2, max = 255, message = "{product_request.title.size}")
        String title,

        @NotNull(message = "{product_request.category.not_null}")
        ProductCategory category,

        @NotNull(message = "{product_request.calories.not_null}")
        @Min(value = 0, message = "{product_request.calories.min}")
        Integer calories,

        @NotNull(message = "{product_request.proteins.not_null}")
        @PositiveOrZero(message = "{product_request.proteins.positive_or_zero}")
        @Digits(integer = 4, fraction = 2, message = "{product_request.digits.invalid}")
        BigDecimal proteins,

        @NotNull(message = "{product_request.fats.not_null}")
        @PositiveOrZero(message = "{product_request.fats.positive_or_zero}")
        @Digits(integer = 4, fraction = 2, message = "{product_request.digits.invalid}")
        BigDecimal fats,

        @NotNull(message = "{product_request.carbohydrates.not_null}")
        @PositiveOrZero(message = "{product_request.carbohydrates.positive_or_zero}")
        @Digits(integer = 4, fraction = 2, message = "{product_request.digits.invalid}")
        BigDecimal carbohydrates,

        List<Long> additiveIds,
        Set<AllergenCategory> allergens,
        Boolean hasPalmOil,
        String compositionText
) {
}