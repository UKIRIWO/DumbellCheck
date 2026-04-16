package com.agg.dumbellcheck.dto;

import java.time.Instant;

public final class WorkoutInfoDTO {

    private WorkoutInfoDTO() {}

    public record EjercicioDto(
        Integer id,
        String nombre,
        String descripcion,
        String grupoMuscular,
        String imagenUrl,
        Instant fechaCreacion
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
}
