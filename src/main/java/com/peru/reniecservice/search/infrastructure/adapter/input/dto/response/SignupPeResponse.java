package com.peru.reniecservice.search.infrastructure.adapter.input.dto.response;

public record SignupPeResponse(
        String username,
        String password,
        String name,
        String email
) {
}
