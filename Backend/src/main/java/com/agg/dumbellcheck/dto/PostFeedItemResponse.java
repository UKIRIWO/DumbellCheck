package com.agg.dumbellcheck.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PostFeedItemResponse(
        Integer id,
        UsuarioResumen usuario,
        String titulo,
        String descripcion,
        String multimediaUrl,
        Integer contadorLikes,
        Integer contadorComentarios,
        Instant fechaCreacion,
        List<EjercicioEnPost> ejercicios
) {

    public record UsuarioResumen(
            Integer id,
            String username,
            String fotoPerfilUrl
    ) {}

    public record EjercicioEnPost(
            Integer id,
            Integer ejercicioId,
            String nombre,
            String imagenUrl,
            Integer orden,
            String notas,
            List<SerieEnPost> series
    ) {}

    public record SerieEnPost(
            Integer numeroSerie,
            Integer repeticiones,
            BigDecimal peso,
            Integer descansoSegundos
    ) {}
}
