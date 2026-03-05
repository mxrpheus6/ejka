package by.kazachenko.ejka.review.model;

import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.user.model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Formula;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"author_id", "product_id"}),
        indexes = {
                @Index(name = "idx_review_product", columnList = "product_id"),
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 4096)
    private String content;

    @Column(check = @CheckConstraint(constraint = "rating >= 1 AND rating <= 5"), nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewVote> votes;

    @Formula("""
    (SELECT COALESCE(SUM(CASE WHEN v.is_upvote = true THEN 1 ELSE -1 END), 0)
    FROM review_votes v WHERE v.review_id = id)
    """)
    @Builder.Default
    private Integer usefulScore = 0;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
