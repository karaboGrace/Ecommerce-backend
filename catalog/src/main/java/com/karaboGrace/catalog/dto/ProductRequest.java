package com.karaboGrace.catalog.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String category
) {}