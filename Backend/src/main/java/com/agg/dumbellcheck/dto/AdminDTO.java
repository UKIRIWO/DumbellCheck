package com.agg.dumbellcheck.dto;

import com.agg.dumbellcheck.entities.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AdminDTO {

    private AdminDTO() {}

    public record AdminUserUpdateRequest(
            RolUsuario rol,
            Boolean estaActivo
    ) {}

    public record AdminPostDto(
            Integer id,
            String publicId,
            String usuarioUsername,
            String titulo,
            Integer contadorLikes,
            Integer contadorComentarios,
            String multimediaUrl,
            boolean estaActiva,
            Instant fechaCreacion
    ) {}

    public record AdminCommentDto(
            Integer id,
            String usuarioUsername,
            String texto,
            Integer publicacionId,
            boolean estaActivo,
            Integer contadorLikes,
            Instant fechaCreacion
    ) {}

    public record AdminBanCreateRequest(
            @NotNull Integer usuarioId,
            @Size(max = 1000) String motivoBaneo,
            Instant baneadoHasta,
            boolean baneadoPermanentemente
    ) {}

    public record AdminEjercicioDto(
            Integer id,
            String nombre,
            String descripcion,
            String imagenUrl,
            Instant fechaCreacion
    ) {}

    public record AdminEjercicioRequest(
            @NotBlank @Size(max = 100) String nombre,
            @Size(max = 5000) String descripcion,
            @Size(max = 255) String imagenUrl
    ) {}

    public record AdminBanUpdateRequest(
            @Size(max = 1000) String motivoBaneo,
            Instant baneadoHasta,
            boolean baneadoPermanentemente
    ) {}
}
