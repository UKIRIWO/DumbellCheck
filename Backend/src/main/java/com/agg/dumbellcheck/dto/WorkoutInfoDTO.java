package com.agg.dumbellcheck.dto;

import java.math.BigDecimal;
import java.time.Instant;

public final class WorkoutInfoDTO {

    private WorkoutInfoDTO() {}

    public record EjercicioDto(
        Integer id,
        String nombre,
        String descripcion,
        String imagenUrl,
        Instant fechaCreacion
    ) {}

    public record GrupoMuscularDto(
        Integer id,
        String nombre,
        String descripcion,
        Instant fechaCreacion
    ) {}

    public record EjercicioGrupoMuscularDto(
        Integer ejercicioId,
        Integer grupoMuscularId
    ) {}

    public record RutinaDto(
        Integer id,
        Integer usuarioId,
        String nombre,
        String descripcion,
        boolean esPublica,
        Instant fechaCreacion
    ) {}

    public record EjercicioRutinaDto(
        Integer id,
        Integer rutinaId,
        Integer ejercicioId,
        Integer orden,
        String notas,
        Instant fechaCreacion
    ) {}

    public record DetalleSerieRutinaDto(
        Integer id,
        Integer ejercicioRutinaId,
        Integer numeroSerie,
        Integer repeticiones,
        BigDecimal peso,
        Integer descansoSegundos,
        Integer rpe,
        String notas,
        Instant fechaCreacion
    ) {}
}
