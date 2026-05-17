package com.agg.dumbellcheck.dto;

import jakarta.validation.constraints.Size;

public record UpdateChatRequest(
        @Size(max = 100) String nombre
) {}
