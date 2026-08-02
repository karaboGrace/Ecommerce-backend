package com.karaboGrace.catalog.service;

import com.karaboGrace.catalog.dto.ProductRequest;
import com.karaboGrace.catalog.dto.ProductResponse;
import com.karaboGrace.catalog.entity.Product;
import com.karaboGrace.catalog.exception.ResourceNotFoundException;
import com.karaboGrace.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // @Cacheable checks Redis first. Cache miss → hits DB and stores result.
    // Cache hit → returns from Redis, DB never touched.
    // Key: "products::all"
    @Cacheable(value = "products", key = "'all'")
    public List<ProductResponse> getAllProducts() {
        System.out.println(">>> CACHE MISS - hitting database for all products");
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    // Key: "products::1", "products::2" etc
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        System.out.println(">>> CACHE MISS - hitting database for product " + id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return ProductResponse.from(product);
    }

    // @CacheEvict removes stale data from Redis when product changes
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .category(request.category())
                .build();
        return ProductResponse.from(productRepository.save(product));
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(request.category());
        return ProductResponse.from(productRepository.save(product));
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}