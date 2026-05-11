package com.agg.dumbellcheck.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RutinaResponse(
        Integer id,
        String publicId,
        String nombre,
        String descripcion,
        boolean esPublica,
        Instant fechaCreacion,
        UsuarioResumen usuario,
        List<EjercicioRutinaResponse> ejercicios
) {

    public record UsuarioResumen(
            Integer id,
            String username,
            String fotoPerfilUrl
    ) {}

    public record EjercicioRutinaResponse(
            Integer id,
            Integer ejercicioId,
            String nombre,
            String imagenUrl,
            Integer orden,
            String notas,
            List<SerieRutinaResponse> series
    ) {}

    public record SerieRutinaResponse(
            Integer numeroSerie,
            Integer repeticiones,
            BigDecimal peso,
            Integer descansoSegundos,
            Integer rpe,
            String notas
    ) {}
}
