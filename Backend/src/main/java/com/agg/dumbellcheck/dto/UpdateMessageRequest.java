package com.agg.dumbellcheck.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMessageRequest(
        @NotBlank String contenido
) {}
