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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAll(Pageable pageable);
    Optional<Product> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.moderationStatus = :status WHERE p.id = :productId")
    int updateModerationStatus(UUID productId, ModerationStatus status);

}
