package com.karaboGrace.catalog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.karaboGrace.catalog.entity.Product;
import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;

    // No-arg constructor required for Jackson deserialization from Redis
    public ProductResponse() {}

    @JsonCreator
    public ProductResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("sku") String sku,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("stockQuantity") Integer stockQuantity,
            @JsonProperty("category") String category
    ) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory()
        );
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public String getCategory() { return category; }
}