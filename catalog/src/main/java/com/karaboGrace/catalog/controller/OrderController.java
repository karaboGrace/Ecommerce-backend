package com.karaboGrace.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaboGrace.catalog.dto.OrderResponse;
import com.karaboGrace.catalog.service.IdempotencyService;
import com.karaboGrace.catalog.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey)
            throws Exception {

        // If client sent an idempotency key, check if we've seen it before
        if (idempotencyKey != null) {
            Optional<String> cached = idempotencyService.getExistingResponse(idempotencyKey);
            if (cached.isPresent()) {
                // Return the exact same response as the first request
                OrderResponse cachedOrder = objectMapper.readValue(
                        cached.get(), OrderResponse.class
                );
                return ResponseEntity.ok(cachedOrder);
            }
        }

        OrderResponse order = orderService.placeOrder(userDetails.getUsername());

        // Store the response so duplicate requests return the same result
        if (idempotencyKey != null) {
            idempotencyService.saveResponse(
                    idempotencyKey,
                    objectMapper.writeValueAsString(order)
            );
        }

        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getMyOrders(userDetails.getUsername()));
    }
}