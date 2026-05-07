package com.agg.dumbellcheck.dto;

import com.agg.dumbellcheck.entities.RolUsuario;

public record AuthLoginResponse(
        String accessToken,
        String refreshToken,
        Integer usuarioId,
        String username,
        RolUsuario rol
) {
}
