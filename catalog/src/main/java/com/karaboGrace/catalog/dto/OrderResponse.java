package com.karaboGrace.catalog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.karaboGrace.catalog.entity.Order;
import com.karaboGrace.catalog.entity.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {

    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<OrderItemResponse> items;

    public OrderResponse() {}

    @JsonCreator
    public OrderResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("status") String status,
            @JsonProperty("totalAmount") BigDecimal totalAmount,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("items") List<OrderItemResponse> items
    ) {
        this.id = id;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.items = items;
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItemResponse> getItems() { return items; }

    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal priceAtPurchase;

        public OrderItemResponse() {}

        @JsonCreator
        public OrderItemResponse(
                @JsonProperty("productId") Long productId,
                @JsonProperty("productName") String productName,
                @JsonProperty("quantity") Integer quantity,
                @JsonProperty("priceAtPurchase") BigDecimal priceAtPurchase
        ) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.priceAtPurchase = priceAtPurchase;
        }

        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getPriceAtPurchase()
            );
        }

        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
    }
}