package com.agg.dumbellcheck.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 120) String apellido1,
        @Size(max = 120) String apellido2,
        @NotBlank @Size(min = 8, max = 120) String password
) {}
