package com.karaboGrace.catalog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);

    public Optional<String> getExistingResponse(String key) {
        Object cached = redisTemplate.opsForValue().get(PREFIX + key);
        if (cached != null) {
            log.info("Idempotency key hit: {} — returning cached response", key);
            return Optional.of(cached.toString());
        }
        return Optional.empty();
    }

    public void saveResponse(String key, String responseJson) {
        redisTemplate.opsForValue().set(PREFIX + key, responseJson, TTL);
        log.info("Idempotency key stored: {}", key);
    }
}