package by.kazachenko.ejka.product.model;

import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductRating;
import by.kazachenko.ejka.review.model.Review;
import by.kazachenko.ejka.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_barcode", columnList = "barcode"),
                @Index(name = "idx_product_title", columnList = "title"),
                @Index(name = "idx_product_creator", columnList = "creator_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(nullable = false)
    private String title;

    private Integer calories;

    private Double proteins;

    private Double fats;

    private Double carbohydrates;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModerationStatus moderationStatus = ModerationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private ProductRating rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    public void updateRating() {
        this.rating = calculateSimplifiedRating(calories, proteins, fats, carbohydrates);
    }

    private ProductRating calculateSimplifiedRating(Integer calories, Double proteins, Double fats, Double carbohydrates) {
        int energyPoints = getPoints(calories, 335);
        int fatPoints = getPoints(fats, 10);
        int carbPoints = getPoints(carbohydrates, 13);

        int negativeScore = energyPoints + fatPoints + carbPoints;

        int proteinPoints = getPoints(proteins, 1.6);
        if (proteinPoints > 5) proteinPoints = 5;

        int finalScore = negativeScore - proteinPoints;

        return mapScoreToGrade(finalScore);
    }

    private int getPoints(Number value, double step) {
        if (value == null) return 0;
        double val = value.doubleValue();
        int points = (int) (val / step);
        return Math.min(points, 10);
    }

    private ProductRating mapScoreToGrade(int score) {
        if (score <= -1) return ProductRating.A;  // Very healthy (Water, simple veggies)
        if (score <= 3)  return ProductRating.B;  // Healthy (Lean meat, complex meals)
        if (score <= 11) return ProductRating.C;  // Moderate (Standard meals)
        if (score <= 16) return ProductRating.D;  // Poor (Fatty/Sugary snacks)
        return ProductRating.E;                   // Unhealthy (Candy, pure fat, fried)
    }

}
