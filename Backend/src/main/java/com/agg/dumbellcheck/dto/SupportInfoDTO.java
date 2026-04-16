package com.agg.dumbellcheck.dto;

import java.time.Instant;
import com.agg.dumbellcheck.entities.EstadoIncidencia;

public final class SupportInfoDTO {

    private SupportInfoDTO() {}

    public record IncidenciaDto(
        Integer id,
        Integer usuarioId,
        Integer chatId,
        String asunto,
        String mensajeInicial,
        EstadoIncidencia estado,
        Instant fechaCreacion
    ) {}
}
