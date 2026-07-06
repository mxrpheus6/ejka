package by.kazachenko.ejka.product.controller;

import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.product.dto.request.ProductRequest;
import by.kazachenko.ejka.product.dto.response.ProductAllResponse;
import by.kazachenko.ejka.product.dto.response.ProductResponse;
import by.kazachenko.ejka.product.model.ProductScore;
import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductCategory;
import by.kazachenko.ejka.product.model.enums.ProductImageType;
import by.kazachenko.ejka.product.service.ProductService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductAllResponse>> getAllProducts(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ModerationStatus status,
            @RequestParam(required = false) Integer minCalories,
            @RequestParam(required = false) Integer maxCalories,
            @RequestParam(required = false) BigDecimal minUserRating,
            @RequestParam(required = false) List<UUID> additiveIds,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) Integer limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {

        PageResponse<ProductAllResponse> productsResponse = productService.getFilteredProducts(
                searchQuery,
                barcode,
                category,
                status,
                minCalories,
                maxCalories,
                minUserRating,
                additiveIds,
                offset,
                limit,
                sortBy,
                sortDirection
        );

        return ResponseEntity.ok(productsResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<ProductAllResponse>> getAllMyProducts(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ModerationStatus status,
            @RequestParam(required = false) Integer minCalories,
            @RequestParam(required = false) Integer maxCalories,
            @RequestParam(required = false) BigDecimal minUserRating,
            @RequestParam(required = false) List<UUID> additiveIds,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) Integer limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {

        PageResponse<ProductAllResponse> productsResponse = productService.getMyFilteredProducts(
                searchQuery,
                barcode,
                category,
                status,
                minCalories,
                maxCalories,
                minUserRating,
                additiveIds,
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

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductAllResponse>> searchProducts(
            @RequestParam @NotBlank String query,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) Integer limit
    ) {
        PageResponse<ProductAllResponse> productsResponse = productService.searchByTextWithRanking(
                query,
                offset,
                limit
        );

        return ResponseEntity.ok(productsResponse);
    }

    @GetMapping(params = "barcode")
    public ResponseEntity<ProductResponse> getProductByBarcode(@RequestParam String barcode) {
        ProductResponse productResponse = productService.getProductByBarcode(barcode);

        return ResponseEntity.ok(productResponse);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("data") @Valid ProductRequest request,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestPart(value = "ingredientsImage", required = false) MultipartFile ingredientsImage,
            @RequestPart(value = "barcodeImage", required = false) MultipartFile barcodeImage
    ) {
        ProductResponse productResponse = productService.createProduct(request, mainImage, ingredientsImage, barcodeImage);
        return ResponseEntity.ok(productResponse);
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID productId,
            @RequestPart("data") @Valid ProductRequest request,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestPart(value = "ingredientsImage", required = false) MultipartFile ingredientsImage,
            @RequestPart(value = "barcodeImage", required = false) MultipartFile barcodeImage,
            @RequestParam(value = "status", required = false) ModerationStatus status
    ) {
        ProductResponse productResponse = productService.updateProduct(productId, request, mainImage, ingredientsImage, barcodeImage, status);

        return ResponseEntity.ok(productResponse);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductById(@PathVariable UUID productId) {
        productService.deleteProductById(productId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadProductImage(
            @PathVariable UUID productId,
            @RequestParam ProductImageType type,
            @RequestPart("file") MultipartFile file
    ) {
        productService.uploadProductImage(productId, type, file);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}/images")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable UUID productId,
            @RequestParam ProductImageType type
    ) {
        productService.deleteProductImage(productId, type);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/analysis")
    public ResponseEntity<ProductScore> getProductScore(@PathVariable UUID productId) {
        ProductScore response = productService.getProductAnalysis(productId);

        return ResponseEntity.ok(response);
    }
}
