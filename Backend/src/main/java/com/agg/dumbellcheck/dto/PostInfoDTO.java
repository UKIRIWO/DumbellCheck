package com.agg.dumbellcheck.dto;

import java.math.BigDecimal;
import java.time.Instant;
import com.agg.dumbellcheck.entities.TipoLike;

public final class PostInfoDTO {

    private PostInfoDTO() {}

    public record PublicacionDto(
        Integer id,
        Integer usuarioId,
        Integer rutinaOrigenId,
        String titulo,
        String descripcion,
        String multimediaUrl,
        Integer contadorLikes,
        Integer contadorComentarios,
        boolean estaActiva,
        Instant fechaCreacion
    ) {}

    public record EjercicioPublicacionDto(
        Integer id,
        Integer publicacionId,
        Integer ejercicioId,
        Integer orden,
        String notas,
        Instant fechaCreacion
    ) {}

    public record DetalleSerieDto(
        Integer id,
        Integer ejercicioPublicacionId,
        Integer numeroSerie,
        Integer repeticiones,
        BigDecimal peso,
        Integer descansoSegundos,
        Integer rpe,
        String notas,
        Instant fechaCreacion
    ) {}

    public record ComentarioDto(
        Integer id,
        Integer publicacionId,
        Integer usuarioId,
        Integer comentarioPadreId,
        String texto,
        Integer contadorLikes,
        boolean estaActivo,
        Instant fechaCreacion
    ) {}

    public record LikeDto(
        Integer id,
        Integer usuarioId,
        TipoLike tipo,
        Integer referenciaId,
        Instant fechaCreacion
    ) {}
}
