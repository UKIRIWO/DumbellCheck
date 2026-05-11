package com.agg.dumbellcheck.dto;

import java.time.Instant;

public record CommentResponse(
        Integer id,
        String texto,
        Integer contadorLikes,
        Instant fechaCreacion,
        UsuarioResumen usuario
) {

    public record UsuarioResumen(
            Integer id,
            String username,
            String fotoPerfilUrl
    ) {}
}
