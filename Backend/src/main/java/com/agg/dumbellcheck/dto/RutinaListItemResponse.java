package com.agg.dumbellcheck.dto;

import java.time.Instant;
import java.util.List;

public record RutinaListItemResponse(
        Integer id,
        String publicId,
        String nombre,
        String descripcion,
        boolean esPublica,
        Instant fechaCreacion,
        UsuarioResumen usuario,
        List<String> nombresEjercicios
) {

    public record UsuarioResumen(
            Integer id,
            String username,
            String fotoPerfilUrl
    ) {}
}
