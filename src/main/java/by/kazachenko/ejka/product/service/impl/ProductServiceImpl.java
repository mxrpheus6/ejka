package by.kazachenko.ejka.product.service.impl;

import by.kazachenko.ejka.additive.model.enums.AllergenCategory;
import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.common.exception.ExceptionMessages;
import by.kazachenko.ejka.common.exception.cutom.ProductAlreadyExistsException;
import by.kazachenko.ejka.common.exception.cutom.ProductNotFoundException;
import by.kazachenko.ejka.common.mapper.PageResponseMapper;
import by.kazachenko.ejka.common.security.SecurityUtils;
import by.kazachenko.ejka.product.dto.request.ProductRequest;
import by.kazachenko.ejka.product.dto.response.ProductAllResponse;
import by.kazachenko.ejka.product.dto.response.ProductResponse;
import by.kazachenko.ejka.product.model.ProductScore;
import by.kazachenko.ejka.product.mapper.ProductMapper;
import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.model.enums.ModerationStatus;
import by.kazachenko.ejka.product.model.enums.ProductCategory;
import by.kazachenko.ejka.product.model.enums.ProductImageType;
import by.kazachenko.ejka.product.rabbitmq.ParsedAdditive;
import by.kazachenko.ejka.product.repository.ProductRepository;
import by.kazachenko.ejka.product.service.ProductImageService;
import by.kazachenko.ejka.product.service.ProductService;
import by.kazachenko.ejka.product.specification.ProductSpecifications;
import by.kazachenko.ejka.user.model.User;
import by.kazachenko.ejka.user.model.enums.Role;
import by.kazachenko.ejka.user.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private final ProductMapper productMapper;
    private final PageResponseMapper pageResponseMapper;

    private final ProductScoringServiceImpl productScoringService;
    private final ProductImageService productImageService;
    private final SecurityUtils securityUtils;


    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductAllResponse> getAllProducts(
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);

        Pageable pageable = PageRequest.of(offset, limit, Sort.by(direction, sortBy));

        Page<ProductAllResponse> responsePage = productRepository
                .findAll(pageable)
                .map(productMapper::toAllResponse);

        return pageResponseMapper.toResponse(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByBarcode(request.barcode())) {
            throw new ProductAlreadyExistsException(ExceptionMessages.PRODUCT_BARCODE_ALREADY_EXISTS);
        }

        Product product = productMapper.toEntity(request);

        UUID loggedUserId = securityUtils.getLoggedUserId();
        User creatorRef = userRepository.getReferenceById(loggedUserId);

        product.setCreator(creatorRef);

        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        if (!product.getBarcode().equals(request.barcode()) && productRepository.existsByBarcode(request.barcode())) {
            throw new ProductAlreadyExistsException(ExceptionMessages.PRODUCT_BARCODE_ALREADY_EXISTS);
        }

        productMapper.updateProductFromDto(request, product);

        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public void deleteProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void deleteProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void changeModerationStatus(UUID productId, ModerationStatus status) {
        int updatedRows = productRepository.updateModerationStatus(productId, status);

        if (updatedRows == 0) {
            throw new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void uploadProductImage(UUID productId, ProductImageType type, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        UUID currentUserId = securityUtils.getLoggedUserId();

        boolean isAdmin = securityUtils.getLoggedUserRole() == Role.ROLE_MODERATOR;

        if (!isAdmin && !product.getCreator().getId().equals(currentUserId)) {
            throw new AccessDeniedException("У вас нет прав на редактирование этого продукта");
        }
        productImageService.uploadImage(product, type, file);
    }

    @Override
    @Transactional
    public void deleteProductImage(UUID productId, ProductImageType type) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        UUID currentUserId = securityUtils.getLoggedUserId();

        boolean isAdmin = securityUtils.getLoggedUserRole() == Role.ROLE_MODERATOR;

        if (!isAdmin && !product.getCreator().getId().equals(currentUserId)) {
            throw new AccessDeniedException("У вас нет прав на редактирование этого продукта");
        }

        productImageService.deleteImage(product, type);
    }

    @Override
    @Transactional
    public void updateProductAnalysis(
            UUID productId,
            List<ParsedAdditive> parsedAdditives,
            Set<AllergenCategory> allergens,
            Boolean hasPalmOil, String parsedText
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        product.setCompositionText(parsedText);
        product.setAllergens(allergens);
        product.setHasPalmOil(hasPalmOil);

        productRepository.deleteAdditivesByProductId(productId);

        if (parsedAdditives != null && !parsedAdditives.isEmpty()) {
            Long[] additiveIdsArray = parsedAdditives.stream()
                    .map(ParsedAdditive::id)
                    .filter(java.util.Objects::nonNull)
                    .toArray(Long[]::new);

            if (additiveIdsArray.length > 0) {
                productRepository.batchInsertAdditives(productId, additiveIdsArray);
            }
        }

        calculateAndSaveScore(product);
    }

    @Override
    @Transactional
    public ProductScore getProductAnalysis(UUID productId) {
        // Достаем продукт из базы.
        // ВАЖНО: Если additives лежат как Lazy, убедись, что они подтянутся,
        // например через @EntityGraph в репозитории, чтобы не словить N+1
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        calculateAndSaveScore(product);
        return productScoringService.calculateScoreDetails(product);
    }

    private void calculateAndSaveScore(Product product) {
        ProductScore scoreDetails = productScoringService.calculateScoreDetails(product);

        product.setNutritionScore(scoreDetails.getTotalScore());
        product.setScoreDetails(scoreDetails);

        productRepository.save(product);
    }

    @Override
    public PageResponse<ProductAllResponse> searchByTextWithRanking(String query, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);

        Page<ProductAllResponse> responsePage = productRepository
                .searchByText(query, pageable)
                .map(productMapper::toAllResponse);

        return pageResponseMapper.toResponse(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductAllResponse> getFilteredProducts(
            String searchQuery,
            String barcode,
            ProductCategory category,
            ModerationStatus status,
            Integer minCalories,
            Integer maxCalories,
            BigDecimal minUserRating,
            List<UUID> additiveIds,
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection)
    {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(offset, limit, Sort.by(direction, sortBy));

        String actualBarcode = barcode;
        String actualSearchQuery = searchQuery;

        if (searchQuery != null && searchQuery.trim().matches("\\d{8,14}")) {
            actualBarcode = searchQuery.trim();
            actualSearchQuery = null;
        }

        Specification<Product> spec = Specification.where(ProductSpecifications.hasModerationStatus(status))
                .and(ProductSpecifications.hasBarcode(actualBarcode))
                .and(ProductSpecifications.titleSimilarTo(actualSearchQuery, 0.25))
                .and(ProductSpecifications.hasCategory(category))
                .and(ProductSpecifications.caloriesBetween(minCalories, maxCalories))
                .and(ProductSpecifications.minUserRating(minUserRating))
                .and(ProductSpecifications.containsAdditives(additiveIds));

        Page<ProductAllResponse> responsePage = productRepository
                .findAll(spec, pageable)
                .map(productMapper::toAllResponse);

        return pageResponseMapper.toResponse(responsePage);
    }

}
