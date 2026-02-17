package by.kazachenko.ejka.product.service.impl;

import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.common.exception.cutom.ProductNotFoundException;
import by.kazachenko.ejka.common.mapper.PageResponseMapper;
import by.kazachenko.ejka.product.dto.request.ProductRequset;
import by.kazachenko.ejka.product.dto.response.ProductResponse;
import by.kazachenko.ejka.product.mapper.ProductMapper;
import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.repository.ProductRepository;
import by.kazachenko.ejka.product.service.ProductService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    private final PageResponseMapper pageResponseMapper;

    @Override
    public PageResponse<ProductResponse> getAllProducts(Integer offset, Integer limit) {
        Page<ProductResponse> responsePage = productRepository
                .findAll(PageRequest.of(offset, limit))
                .map(productMapper::toResponse);

        return pageResponseMapper.toResponse(responsePage);
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(ProductNotFoundException::new);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequset request) {
        Product product = productMapper.toEntity(request);

        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productId, ProductRequset request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        productMapper.updateProductFromDto(request, product);

        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public void deleteProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void deleteProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(ProductNotFoundException::new);

        productRepository.delete(product);
    }
}
