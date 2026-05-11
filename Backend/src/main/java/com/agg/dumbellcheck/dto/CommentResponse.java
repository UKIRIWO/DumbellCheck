package com.agg.dumbellcheck.dto;

import java.time.Instant;
import java.util.List;

public record CommentResponse(
        Integer id,
        Integer comentarioPadreId,
        String texto,
        Integer contadorLikes,
        Instant fechaCreacion,
        UsuarioResumen usuario,
        List<CommentResponse> respuestas
) {

    public record UsuarioResumen(
            Integer id,
            String username,
            String fotoPerfilUrl
    ) {}
}
