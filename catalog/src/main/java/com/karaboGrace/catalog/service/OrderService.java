package com.karaboGrace.catalog.service;

import com.karaboGrace.catalog.dto.OrderResponse;
import com.karaboGrace.catalog.entity.*;
import com.karaboGrace.catalog.exception.InsufficientStockException;
import com.karaboGrace.catalog.exception.ResourceNotFoundException;
import com.karaboGrace.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // @Transactional means ALL of this runs as one atomic operation.
    // If anything fails halfway through, the entire thing rolls back.
    // No half-placed orders, no stock decremented without an order created.
    @Transactional
    public OrderResponse placeOrder(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;
        Order order = Order.builder()
                .user(user)
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (CartItem cartItem : cartItems) {
            // Re-fetch the product inside the transaction with a fresh read.
            // The @Version field is checked here — if another transaction
            // already modified this product, JPA throws OptimisticLockException.
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for: " + product.getName() +
                                ". Available: " + product.getStockQuantity() +
                                ", Requested: " + cartItem.getQuantity()
                );
            }

            // Decrement stock — @Version increments automatically,
            // preventing another concurrent transaction from also decrementing
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        // Clear the cart after successful order
        cartItemRepository.deleteByUser(user);

        return OrderResponse.from(savedOrder);
    }

    public List<OrderResponse> getMyOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUser(user)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }
}