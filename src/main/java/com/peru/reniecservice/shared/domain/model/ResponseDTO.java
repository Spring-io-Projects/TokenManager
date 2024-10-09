package com.peru.reniecservice.shared.domain.model;

import jakarta.validation.constraints.NotBlank;

public record ResponseDTO(
        @NotBlank String message,

        @NotBlank String lastDni,

        @NotBlank String countSuccess,

        @NotBlank String countError,

        @NotBlank String countInvalid
) {
}
