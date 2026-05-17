package com.agg.dumbellcheck.dto;

import java.time.Instant;
import java.util.List;

public record ChatListItemResponse(
        String publicId,
        String nombre,
        String tipo,
        String fotoUrl,
        UltimoMensajePreview ultimoMensaje,
        int mensajesNoLeidos,
        Instant fechaUltimaActividad,
        List<ParticipanteResumen> participantes
) {
    public record UltimoMensajePreview(
            String tipoMensaje,
            String contenido,
            String remitenteUsername,
            boolean esMio,
            Instant fechaCreacion
    ) {}

    public record ParticipanteResumen(
            Integer usuarioId,
            String username,
            String fotoPerfilUrl
    ) {}
}
