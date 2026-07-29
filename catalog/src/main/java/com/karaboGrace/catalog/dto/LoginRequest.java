package com.karaboGrace.catalog.dto;

public record LoginRequest(
        String email,
        String password
) {}