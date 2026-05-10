package com.maybank.demo.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maybank.demo.dto.PageResponse;
import com.maybank.demo.dto.ProductFilter;
import com.maybank.demo.dto.ProductRequest;
import com.maybank.demo.dto.ProductResponse;
import com.maybank.demo.exception.ResourceNotFoundException;
import com.maybank.demo.model.Product;
import com.maybank.demo.repository.ProductRepository;
import com.maybank.demo.specification.ProductSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String CACHE_PRODUCT  = "product";
    private static final String CACHE_PRODUCTS = "products";

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PRODUCT, key = "#id")
    public ProductResponse getProduct(Long id) {
        log.debug("Fetching product with id: {}", id);

        try {
            ProductResponse response = productRepository.findById(id)
                    .map(ProductResponse::new)
                    .orElseThrow(() -> {
                        log.warn("Product not found with id: {}", id);
                        return new ResourceNotFoundException("Product not found with id: " + id);
                    });

            log.info("Successfully retrieved product with id: {}", id);
            return response;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving product with id: {}", id, e);
            throw new RuntimeException("Failed to retrieve product", e);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PRODUCTS, key = "#page + '-' + #size")
    public PageResponse<ProductResponse> getAllProducts(int page, int size) {
        log.debug("Fetching all products - page: {}, size: {}", page, size);
        return fetchPage(page, size, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(int page, int size, ProductFilter filter) {
        log.debug("Fetching filtered products - page: {}, size: {}, filter: {}", page, size, filter);
        return fetchPage(page, size, filter);
    }

    @Transactional
    @CacheEvict(value = CACHE_PRODUCTS, allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product with name: {}", request.getName());

        try {
            Product product = Product.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .build();

            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully with id: {}", savedProduct.getId());

            return new ProductResponse(savedProduct);
        } catch (Exception e) {
            log.error("Error creating product with name: {}", request.getName(), e);
            throw new RuntimeException("Failed to create product", e);
        }
    }

    @Transactional
    @Caching(
        put   = @CachePut(value = CACHE_PRODUCT, key = "#id"),
        evict = @CacheEvict(value = CACHE_PRODUCTS, allEntries = true)
    )
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with id: {}", id);

        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Product not found for update with id: {}", id);
                        return new ResourceNotFoundException("Product not found with id: " + id);
                    });

            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setQuantity(request.getQuantity());

            Product updatedProduct = productRepository.save(product);
            log.info("Product updated successfully with id: {}", id);

            return new ProductResponse(updatedProduct);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating product with id: {}", id, e);
            throw new RuntimeException("Failed to update product", e);
        }
    }

    // ----------------------------------------

    private PageResponse<ProductResponse> fetchPage(int page, int size, ProductFilter filter) {
        try {
            Specification<Product> spec = ProductSpecification.from(filter);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Product> result = productRepository.findAll(spec, pageRequest);

            log.debug("Retrieved {} products from page {}", result.getNumberOfElements(), page);

            return new PageResponse<>(
                    result.getContent().stream().map(ProductResponse::new).toList(),
                    result.getNumber(),
                    result.getSize(),
                    result.getTotalElements(),
                    result.getTotalPages()
            );
        } catch (Exception e) {
            log.error("Error fetching products page: {}, size: {}", page, size, e);
            throw new RuntimeException("Failed to fetch products", e);
        }
    }
}
