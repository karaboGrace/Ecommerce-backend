package com.karaboGrace.catalog.service;

import com.karaboGrace.catalog.dto.CartItemResponse;
import com.karaboGrace.catalog.entity.CartItem;
import com.karaboGrace.catalog.entity.Product;
import com.karaboGrace.catalog.entity.User;
import com.karaboGrace.catalog.exception.ResourceNotFoundException;
import com.karaboGrace.catalog.repository.CartItemRepository;
import com.karaboGrace.catalog.repository.ProductRepository;
import com.karaboGrace.catalog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<CartItemResponse> getCart(String email) {
        User user = getUser(email);
        return cartItemRepository.findByUser(user)
                .stream()
                .map(CartItemResponse::from)
                .toList();
    }

    @Transactional
    public CartItemResponse addToCart(String email, Long productId, Integer quantity) {
        User user = getUser(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // If item already in cart, increase quantity
        CartItem cartItem = cartItemRepository
                .findByUserAndProductId(user, productId)
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + quantity);
                    return existing;
                })
                .orElse(CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(quantity)
                        .build());

        return CartItemResponse.from(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeFromCart(String email, Long cartItemId) {
        User user = getUser(email);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Cart item not found: " + cartItemId);
        }
        cartItemRepository.delete(item);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}