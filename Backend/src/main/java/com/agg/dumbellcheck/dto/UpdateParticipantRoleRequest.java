package com.agg.dumbellcheck.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateParticipantRoleRequest(
        @NotBlank
        @Pattern(regexp = "^(admin|miembro)$")
        String rol
) {}
