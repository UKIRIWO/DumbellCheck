package com.agg.dumbellcheck.dto;

import java.util.List;

public record ChatDetailResponse(
        String publicId,
        String nombre,
        String tipo,
        String fotoUrl,
        boolean esGrupo,
        boolean soyAdmin,
        List<ParticipanteDto> participantes
) {
    public record ParticipanteDto(
            Integer usuarioId,
            String username,
            String nombre,
            String fotoPerfilUrl,
            String rol
    ) {}
}
