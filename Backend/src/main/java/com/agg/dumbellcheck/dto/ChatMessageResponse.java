package com.agg.dumbellcheck.dto;

import java.time.Instant;

public record ChatMessageResponse(
        Integer id,
        String chatPublicId,
        Integer usuarioId,
        String username,
        String fotoPerfilUrl,
        String tipoMensaje,
        String contenido,
        String archivoUrl,
        Integer rutinaId,
        String rutinaNombre,
        String rutinaPublicId,
        Integer mensajeReferenciaId,
        String mensajeReferenciaPreview,
        String mensajeReferenciaUsername,
        boolean estaEditado,
        boolean eliminado,
        boolean esMio,
        Instant fechaCreacion,
        Instant fechaEdicion
) {}
