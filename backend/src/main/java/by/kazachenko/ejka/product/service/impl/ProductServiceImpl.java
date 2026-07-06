package by.kazachenko.ejka.product.service.impl;

import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.additive.model.enums.AllergenCategory;
import by.kazachenko.ejka.additive.repository.AdditiveRepository;
import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.common.exception.ExceptionMessages;
import by.kazachenko.ejka.common.exception.custom.ProductAlreadyExistsException;
import by.kazachenko.ejka.common.exception.custom.ProductNotFoundException;
import by.kazachenko.ejka.common.exception.custom.ProductPendingException;
import by.kazachenko.ejka.common.exception.custom.TooManyPendingProductsException;
import by.kazachenko.ejka.common.mapper.PageResponseMapper;
import by.kazachenko.ejka.common.security.SecurityUtils;
import by.kazachenko.ejka.product.dto.request.ProductRequest;
import by.kazachenko.ejka.product.dto.response.ProductAllResponse;
import by.kazachenko.ejka.product.dto.response.ProductResponse;
import by.kazachenko.ejka.product.model.ProductImage;
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
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AdditiveRepository additiveRepository;

    private final ProductMapper productMapper;
    private final PageResponseMapper pageResponseMapper;

    private final ProductScoringServiceImpl productScoringService;
    private final ProductImageService productImageService;
    private final SecurityUtils securityUtils;

    private static final int MAX_PENDING_PRODUCTS = 5;


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
    @Cacheable(value = "productsById", key = "#id", unless = "#result.moderationStatus.name() != 'APPROVED'")
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        checkProductViewAccess(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productsByBarcode", key = "#barcode", unless = "#result.moderationStatus.name() != 'APPROVED'")
    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        checkProductViewAccess(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, MultipartFile mainImg, MultipartFile ingImg, MultipartFile barcodeImg) {
        UUID loggedUserId = securityUtils.getLoggedUserId();
        Role userRole = securityUtils.getLoggedUserRole();

        if (userRole != Role.ROLE_MODERATOR) {
            long pendingCount = productRepository.countByCreatorIdAndModerationStatus(loggedUserId, ModerationStatus.PENDING);
            if (pendingCount >= MAX_PENDING_PRODUCTS) {
                throw new TooManyPendingProductsException("Вы достигли лимита. Пожалуйста, дождитесь проверки ваших предыдущих товаров.");
            }
        }

        Optional<Product> existingOpt = productRepository.findByBarcode(request.barcode());
        if (existingOpt.isPresent()) {
            Product existing = existingOpt.get();

            if (existing.getModerationStatus() == ModerationStatus.APPROVED) {
                throw new ProductAlreadyExistsException(ExceptionMessages.PRODUCT_BARCODE_ALREADY_EXISTS);

            } else if (existing.getModerationStatus() == ModerationStatus.PENDING) {
                throw new ProductPendingException("Этот продукт уже добавлен и ожидает проверки модератором.");

            } else if (existing.getModerationStatus() == ModerationStatus.REJECTED) {
                deleteProductWithImages(existing);
                productRepository.flush();
            }
        }

        Product product = productMapper.toEntity(request);

        User creatorRef = userRepository.getReferenceById(loggedUserId);
        product.setCreator(creatorRef);

        if (userRole == Role.ROLE_MODERATOR) {
            product.setModerationStatus(ModerationStatus.APPROVED);
        } else {
            product.setModerationStatus(ModerationStatus.PENDING);
        }

        product.setCompositionText(request.compositionText());
        product.setAllergens(request.allergens());
        product.setHasPalmOil(request.hasPalmOil());
        product.setCreatedAt(Instant.now());

        product = productRepository.save(product);

        assignAdditivesToProduct(product, request.additiveIds());

        if (mainImg != null) productImageService.uploadImage(product, ProductImageType.MAIN, mainImg);
        if (ingImg != null) productImageService.uploadImage(product, ProductImageType.INGREDIENTS, ingImg);
        if (barcodeImg != null) productImageService.uploadImage(product, ProductImageType.BARCODE, barcodeImg);

        calculateAndSaveScore(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productsById", key = "#productId"),
            @CacheEvict(value = "productsByBarcode", allEntries = true)
    })
    public ProductResponse updateProduct(
            UUID productId,
            ProductRequest request,
            MultipartFile mainImg,
            MultipartFile ingImg,
            MultipartFile barcodeImg,
            ModerationStatus status
    ) {
        Role userRole = securityUtils.getLoggedUserRole();
        if (userRole != Role.ROLE_MODERATOR) {
            throw new AccessDeniedException("Редактировать продукты может только модератор.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        if (!product.getBarcode().equals(request.barcode()) && productRepository.existsByBarcode(request.barcode())) {
            throw new ProductAlreadyExistsException(ExceptionMessages.PRODUCT_BARCODE_ALREADY_EXISTS);
        }

        productMapper.updateProductFromDto(request, product);

        product.setCompositionText(request.compositionText());
        product.setAllergens(request.allergens());
        product.setHasPalmOil(request.hasPalmOil());

        if (status != null) {
            product.setModerationStatus(status);
        }

        assignAdditivesToProduct(product, request.additiveIds());

        if (mainImg != null) productImageService.uploadImage(product, ProductImageType.MAIN, mainImg);
        if (ingImg != null) productImageService.uploadImage(product, ProductImageType.INGREDIENTS, ingImg);
        if (barcodeImg != null) productImageService.uploadImage(product, ProductImageType.BARCODE, barcodeImg);

        calculateAndSaveScore(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productsById", key = "#id"),
            @CacheEvict(value = "productsByBarcode", allEntries = true)
    })
    public void deleteProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND));

        Role userRole = securityUtils.getLoggedUserRole();

        if (userRole != Role.ROLE_MODERATOR) {
            throw new AccessDeniedException("Удалять продукты может только модератор");
        }

        deleteProductWithImages(product);
    }

    private void checkProductViewAccess(Product product) {
        if (product.getModerationStatus() == ModerationStatus.APPROVED) {
            return;
        }

        UUID currentUserId = securityUtils.getLoggedUserId();
        Role currentUserRole = securityUtils.getLoggedUserRole();

        if (currentUserId == null) {
            throw new AccessDeniedException("Этот продукт находится на модерации. Доступ запрещен.");
        }

        boolean isModeratorOrAdmin = (currentUserRole == Role.ROLE_MODERATOR);
        boolean isCreator = product.getCreator().getId().equals(currentUserId);

        if (!isModeratorOrAdmin && !isCreator) {
            throw new AccessDeniedException("У вас нет прав для просмотра этого продукта, так как он еще не прошел модерацию.");
        }
    }

    private void deleteProductWithImages(Product product) {
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<ProductImageType> imageTypes = product.getImages().stream()
                    .map(ProductImage::getType)
                    .toList();

            for (ProductImageType type : imageTypes) {
                productImageService.deleteImage(product, type);
            }
        }

        productRepository.delete(product);
    }

    private void assignAdditivesToProduct(Product product, List<Long> additiveIds) {
        if (additiveIds != null && !additiveIds.isEmpty()) {
            Set<Additive> additives = new LinkedHashSet<>(additiveRepository.findAllById(additiveIds));
            product.setAdditives(additives);
        } else {
            product.getAdditives().clear();
        }
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
        Role userRole = securityUtils.getLoggedUserRole();
        boolean isPrivileged = (userRole == Role.ROLE_MODERATOR);

        if (!isPrivileged) {
            if (status != null && status != ModerationStatus.APPROVED) {
                throw new AccessDeniedException("Доступ к продуктам со статусом " + status + " запрещен.");
            }
            status = ModerationStatus.APPROVED;
        }

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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductAllResponse> getMyFilteredProducts(
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
        UUID currentUserId = securityUtils.getLoggedUserId();

        if (currentUserId == null) {
            throw new InsufficientAuthenticationException("Для просмотра своих продуктов необходимо авторизоваться.");
        }

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(offset, limit, Sort.by(direction, sortBy));

        String actualBarcode = barcode;
        String actualSearchQuery = searchQuery;

        if (searchQuery != null && searchQuery.trim().matches("\\d{8,14}")) {
            actualBarcode = searchQuery.trim();
            actualSearchQuery = null;
        }

        Specification<Product> spec = Specification.where(ProductSpecifications.hasCreatorId(currentUserId))
                .and(ProductSpecifications.hasModerationStatus(status))
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
