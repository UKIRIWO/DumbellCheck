package com.agg.dumbellcheck.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequest(
        @NotBlank @Size(max = 120) String principal,
        @NotBlank @Size(min = 8, max = 120) String password
) {}
