package com.karaboGrace.catalog.dto;

import com.karaboGrace.catalog.entity.CartItem;
import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        Integer quantity,
        BigDecimal priceEach,
        BigDecimal subtotal
) {
    public static CartItemResponse from(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSku(),
                item.getQuantity(),
                item.getProduct().getPrice(),
                subtotal
        );
    }
}