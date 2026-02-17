package by.kazachenko.ejka.product.repository;

import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.model.ProductImage;
import by.kazachenko.ejka.product.model.enums.ProductImageType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    Optional<ProductImage> findByProductAndType(Product product, ProductImageType type);

}
