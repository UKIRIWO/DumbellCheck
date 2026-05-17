package com.agg.dumbellcheck.dto;

public record ChatSearchUserResponse(
        Integer id,
        String username,
        String nombre,
        String fotoPerfilUrl,
        boolean sigo
) {}
