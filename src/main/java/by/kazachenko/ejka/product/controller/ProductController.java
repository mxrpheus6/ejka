package by.kazachenko.ejka.product.controller;

import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.product.dto.request.ProductRequest;
import by.kazachenko.ejka.product.dto.response.ProductResponse;
import by.kazachenko.ejka.product.service.ProductImageService;
import by.kazachenko.ejka.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponse<ProductResponse> productsResponse = productService.getAllProducts(
                offset,
                limit,
                sortBy,
                sortDirection
        );

        return ResponseEntity.ok(productsResponse);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID productId) {
        ProductResponse productResponse = productService.getProductById(productId);

        return ResponseEntity.ok(productResponse);
    }

    @GetMapping(params = "barcode")
    public ResponseEntity<ProductResponse> getProductByBarcode(@RequestParam String barcode) {
        ProductResponse productResponse = productService.getProductByBarcode(barcode);

        return ResponseEntity.ok(productResponse);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        ProductResponse productResponse = productService.createProduct(request);

        return ResponseEntity.ok(productResponse);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID productId,
            @RequestBody ProductRequest request
    ) {
        ProductResponse productResponse = productService.updateProduct(productId, request);

        return ResponseEntity.ok(productResponse);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductById(@PathVariable UUID productId) {
        productService.deleteProductById(productId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(params = "barcode")
    public ResponseEntity<Void> deleteProductByBarcode(@RequestParam String barcode) {
        productService.deleteProductByBarcode(barcode);

        return ResponseEntity.noContent().build();
    }

}
