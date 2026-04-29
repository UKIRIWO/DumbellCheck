package com.agg.dumbellcheck.dto;

import java.time.Instant;
import java.util.List;

import com.agg.dumbellcheck.entities.RolUsuario;
import com.agg.dumbellcheck.entities.TipoNotificacion;

public final class UserInfoDTO {

    private UserInfoDTO() {}

    public record BaneoDto(
        Integer id,
        Integer usuarioId,
        Instant baneadoHasta,
        boolean baneadoPermanentemente,
        String motivoBaneo,
        Instant fechaCreacion
    ) {}

    public record UsuarioDto(
        Integer id,
        String username,
        String email,
        String nombre,
        String apellido1,
        String apellido2,
        String fotoPerfilUrl,
        String biografia,
        RolUsuario rol,
        Integer contadorSeguidores,
        Integer contadorSeguidos,
        List<BaneoDto> baneos,
        boolean estaActivo,
        Instant fechaCreacion,
        Instant ultimaConexion
    ) {}

    public record SeguidorDto(
        Integer id,
        Integer usuarioId,
        Integer seguidoId,
        Instant fechaSeguimiento
    ) {}

    public record SidebarProfileDto(
        Integer id,
        String username,
        String nombre,
        String fotoPerfilUrl,
        Integer contadorSeguidores,
        Integer contadorSeguidos
    ) {}

    public record SidebarSuggestionDto(
        Integer id,
        String username,
        String nombre,
        String fotoPerfilUrl
    ) {}

    public record SidebarDataDto(
        SidebarProfileDto perfil,
        List<SidebarSuggestionDto> sugerencias
    ) {}

    public record NotificacionDto(
        Integer id,
        Integer usuarioId,
        TipoNotificacion tipo,
        Integer referenciaId,
        boolean leida,
        Instant fechaCreacion
    ) {}
}