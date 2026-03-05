package by.kazachenko.ejka.product.repository;

import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.model.enums.ModerationStatus;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAll(Pageable pageable);

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    @Modifying
    @Query(value = """
    UPDATE products 
    SET user_rating = COALESCE((SELECT ROUND(AVG(r.rating * 1.0), 2) FROM reviews r WHERE r.product_id = :productId), 0.0),
        reviews_count = (SELECT COUNT(r.id) FROM reviews r WHERE r.product_id = :productId)
    WHERE id = :productId
    """, nativeQuery = true)
    void recalculateAndUpdateUserRating(@Param("productId") UUID productId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM product_additives WHERE product_id = :productId", nativeQuery = true)
    void deleteAdditivesByProductId(@Param("productId") UUID productId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO product_additives (product_id, additive_id) " +
            "SELECT :productId, unnest(:additiveIds)", nativeQuery = true)
    void batchInsertAdditives(
            @Param("productId") UUID productId,
            @Param("additiveIds") Long[] additiveIds
    );

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.moderationStatus = :status WHERE p.id = :productId")
    int updateModerationStatus(UUID productId, ModerationStatus status);

}
