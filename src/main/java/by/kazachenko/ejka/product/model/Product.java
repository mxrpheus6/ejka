package by.kazachenko.ejka.product.model;

import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.additive.model.enums.AllergenCategory;
import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductCategory;
import by.kazachenko.ejka.review.model.Review;
import by.kazachenko.ejka.user.model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_barcode", columnList = "barcode"),
                @Index(name = "idx_product_creator", columnList = "creator_id"),
                @Index(name = "idx_product_created_at", columnList = "createdAt")
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    private Integer calories;

    @Column(precision = 6, scale = 2, nullable = false)
    private BigDecimal proteins;

    @Column(precision = 6, scale = 2, nullable = false)
    private BigDecimal fats;

    @Column(precision = 6, scale = 2, nullable = false)
    private BigDecimal carbohydrates;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.PENDING;

    private Integer nutritionScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_details", columnDefinition = "jsonb")
    private ProductScore scoreDetails;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @Column(name = "user_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal userRating = BigDecimal.valueOf(0.0);

    @Column(name = "reviews_count")
    @Builder.Default
    private Integer reviewsCount = 0;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String compositionText;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_additives",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "additive_id")
    )
    @OrderBy("code ASC")
    @Builder.Default
    private Set<Additive> additives = new LinkedHashSet<>();

    @ElementCollection(targetClass = AllergenCategory.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "product_allergens",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen")
    @Builder.Default
    private Set<AllergenCategory> allergens = new HashSet<>();

    @Column(name = "has_palm_oil")
    private Boolean hasPalmOil;

}
