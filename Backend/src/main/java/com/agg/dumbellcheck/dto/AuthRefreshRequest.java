package com.agg.dumbellcheck.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRefreshRequest(
        @NotBlank String refreshToken
) {
}
