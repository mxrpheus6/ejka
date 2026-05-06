package by.kazachenko.ejka.product.service.impl;

import by.kazachenko.ejka.common.service.impl.MinioServiceImpl;
import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.model.ProductImage;
import by.kazachenko.ejka.product.model.enums.ProductImageType;
import by.kazachenko.ejka.product.rabbitmq.ImagePublisher;
import by.kazachenko.ejka.product.repository.ProductImageRepository;
import by.kazachenko.ejka.product.service.ProductImageService;

import java.util.Optional;

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

    private final ImagePublisher imagePublisher;

    @Value("${minio.buckets.products}")
    private String productsBucketName;

    @Override
    public String generatePresignedUrl(String objectKey) {
        return minioService.getFileUrl(productsBucketName, objectKey);
    }

    @Transactional
    public void uploadImage(Product product, ProductImageType type, MultipartFile file) {
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

    @Override
    @Transactional
    public void deleteImage(Product product, ProductImageType type) {
        ProductImage existingImage = productImageRepository.findByProductAndType(product, type)
                .orElseThrow(() -> new RuntimeException("Фотография не найдена"));

        String objectKey = existingImage.getObjectKey();

        productImageRepository.delete(existingImage);

        minioService.deleteFile(productsBucketName, objectKey);
    }

}
