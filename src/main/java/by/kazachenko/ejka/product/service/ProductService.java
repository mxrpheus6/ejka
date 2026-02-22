package by.kazachenko.ejka.product.service;

import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.product.dto.request.ProductRequest;
import by.kazachenko.ejka.product.dto.response.ProductResponse;
import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import java.util.UUID;

public interface ProductService {

    PageResponse<ProductResponse> getAllProducts(Integer offset, Integer limit, String sortBy, String sortDirection);
    ProductResponse getProductById(UUID id);
    ProductResponse getProductByBarcode(String barcode);

    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(UUID productId, ProductRequest request);

    void deleteProductById(UUID id);
    void deleteProductByBarcode(String barcode);

    void changeModerationStatus(UUID productId, ModerationStatus status);

}
