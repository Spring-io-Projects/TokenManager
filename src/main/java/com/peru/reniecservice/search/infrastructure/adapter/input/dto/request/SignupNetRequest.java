package com.peru.reniecservice.search.infrastructure.adapter.input.dto.request;

public record SignupNetRequest(
        String username,
        String password,
        String name,
        String email
) {
}
