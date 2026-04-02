package by.kazachenko.ejka.product.service;

import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.product.dto.request.ProductRequest;
import by.kazachenko.ejka.product.dto.response.ProductAllResponse;
import by.kazachenko.ejka.product.dto.response.ProductResponse;
import by.kazachenko.ejka.product.dto.response.ProductScoreResponse;
import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductImageType;

import by.kazachenko.ejka.product.rabbitmq.ParsedAdditive;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    PageResponse<ProductAllResponse> getAllProducts(Integer offset, Integer limit, String sortBy, String sortDirection);

    ProductResponse getProductById(UUID id);

    ProductResponse getProductByBarcode(String barcode);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(UUID productId, ProductRequest request);

    void deleteProductById(UUID id);

    void deleteProductByBarcode(String barcode);

    void uploadProductImage(UUID productId, ProductImageType type, MultipartFile file);

    void deleteProductImage(UUID productId, ProductImageType type);

    void changeModerationStatus(UUID productId, ModerationStatus status);

    void updateProductAdditives(UUID productId, List<ParsedAdditive> parsedAdditives, String parsedText);

    ProductScoreResponse getProductAnalysis(UUID productId);

}
