package com.agg.dumbellcheck.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record RutinaCreateRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotEmpty @Valid List<EjercicioRutinaRequest> ejercicios
) {

    public record EjercicioRutinaRequest(
            @NotNull Integer ejercicioId,
            Integer orden,
            String notas,
            @NotEmpty @Valid List<SerieRutinaRequest> series
    ) {}

    public record SerieRutinaRequest(
            @NotNull @Positive Integer numeroSerie,
            @NotNull @Positive Integer repeticiones,
            @NotNull @DecimalMin("0.0") BigDecimal peso,
            @Min(0) Integer descansoSegundos,
            @Min(1) @Max(10) Integer rpe,
            String notas
    ) {}
}
