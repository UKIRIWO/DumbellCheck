package com.agg.dumbellcheck.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PostCreateRequest(
        @NotBlank String titulo,
        String descripcion,
        String multimediaUrl,
        Integer rutinaOrigenId,
        @NotEmpty @Valid List<EjercicioRequest> ejercicios
) {

    public record EjercicioRequest(
            @NotNull Integer ejercicioId,
            Integer orden,
            String notas,
            @NotEmpty @Valid List<SerieRequest> series
    ) {}

    public record SerieRequest(
            @NotNull @Positive Integer numeroSerie,
            @NotNull @Positive Integer repeticiones,
            @NotNull @DecimalMin("0.0") BigDecimal peso,
            @Min(0) Integer descansoSegundos,
            @Min(1) @Max(10) Integer rpe,
            String notas
    ) {}
}
