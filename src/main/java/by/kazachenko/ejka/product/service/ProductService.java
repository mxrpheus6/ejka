package by.kazachenko.ejka.product.service;

import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.product.dto.request.ProductRequset;
import by.kazachenko.ejka.product.dto.response.ProductResponse;
import java.util.UUID;

public interface ProductService {

    PageResponse<ProductResponse> getAllProducts(Integer offset, Integer limit);
    ProductResponse getProductById(UUID id);
    ProductResponse getProductByBarcode(String barcode);

    ProductResponse createProduct(ProductRequset request);
    ProductResponse updateProduct(UUID productId, ProductRequset request);

    void deleteProductById(UUID id);
    void deleteProductByBarcode(String barcode);



}
