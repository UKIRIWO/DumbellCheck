package com.agg.dumbellcheck.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank String tipoMensaje,
        String contenido,
        String archivoUrl,
        Integer rutinaId,
        Integer mensajeReferenciaId
) {}
