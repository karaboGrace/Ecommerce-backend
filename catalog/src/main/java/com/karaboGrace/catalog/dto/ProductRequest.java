package com.karaboGrace.catalog.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must be 50 characters or less")
        String sku,

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be 200 characters or less")
        String name,

        @Size(max = 1000, message = "Description must be 1000 characters or less")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Price format invalid")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        @NotBlank(message = "Category is required")
        @Size(max = 80, message = "Category must be 80 characters or less")
        String category
) {}