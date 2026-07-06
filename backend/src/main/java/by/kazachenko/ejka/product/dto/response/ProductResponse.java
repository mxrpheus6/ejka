package by.kazachenko.ejka.product.dto.response;

import by.kazachenko.ejka.additive.dto.response.AdditiveResponse;
import by.kazachenko.ejka.additive.model.enums.AllergenCategory;
import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private String barcode;
    private String title;
    private String compositionText;
    private ProductCategory category;
    private List<ProductImageResponse> images;
    private Integer nutritionScore;
    private ProductScoreResponse scoreDetails;
    private BigDecimal userRating;
    private Integer reviewsCount;
    private Instant createdAt;
    private ModerationStatus moderationStatus;
    private Integer calories;
    private BigDecimal proteins;
    private BigDecimal fats;
    private BigDecimal carbohydrates;
    private Set<AdditiveResponse> additives;
    private Set<AllergenCategory> allergens;
    private Boolean hasPalmOil;
    private String authorUsername;
}
