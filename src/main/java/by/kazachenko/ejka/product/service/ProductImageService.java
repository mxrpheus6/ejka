package by.kazachenko.ejka.product.service;

import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.model.enums.ProductImageType;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageService {

    String generatePresignedUrl(String objectKey);

    void uploadImage(Product product, ProductImageType type, MultipartFile file);

    void deleteImage(Product product, ProductImageType type);

}
