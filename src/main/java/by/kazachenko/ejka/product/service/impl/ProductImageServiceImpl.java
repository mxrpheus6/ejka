package by.kazachenko.ejka.product.service.impl;

import by.kazachenko.ejka.common.exception.cutom.ProductNotFoundException;
import by.kazachenko.ejka.common.service.impl.MinioServiceImpl;
import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.model.ProductImage;
import by.kazachenko.ejka.product.model.enums.ProductImageType;
import by.kazachenko.ejka.product.repository.ProductImageRepository;
import by.kazachenko.ejka.product.repository.ProductRepository;
import by.kazachenko.ejka.product.service.ProductImageService;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final MinioServiceImpl minioService;
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Value("${minio.buckets.products}")
    private String productsBucketName;

    @Transactional
    public void uploadImage(UUID productId, ProductImageType type, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Продукт не найден"));

        String newObjectKey = minioService.uploadFile(file, productsBucketName);

        Optional<ProductImage> optionalProductImage = productImageRepository
                .findByProductAndType(product, type);

        if (optionalProductImage.isPresent()) {
            ProductImage existingImage = optionalProductImage.get();
            String oldObjectKey = existingImage.getObjectKey();

            existingImage.setObjectKey(newObjectKey);
            existingImage.setContentType(file.getContentType());
            productImageRepository.save(existingImage);

            minioService.deleteFile(productsBucketName, oldObjectKey);
        } else {
            ProductImage newImage = ProductImage.builder()
                    .product(product)
                    .type(type)
                    .objectKey(newObjectKey)
                    .contentType(file.getContentType())
                    .build();

            productImageRepository.save(newImage);
        }
    }

}
