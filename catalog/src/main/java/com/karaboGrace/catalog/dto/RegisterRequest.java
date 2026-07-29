package com.karaboGrace.catalog.dto;

public record RegisterRequest(
        String email,
        String password,
        String fullName
) {}