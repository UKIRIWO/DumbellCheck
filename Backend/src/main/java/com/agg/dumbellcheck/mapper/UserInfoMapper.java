/*
usuarios (Perfil, auth, estadísticas de seguidores).

baneos (Historial de sanciones por usuario).

seguidores (Relación seguidor/seguido).
notificaciones (Alertas de actividad social).
*/

package com.agg.dumbellcheck.mapper;

import com.agg.dumbellcheck.dto.UserInfoDTO.*;
import com.agg.dumbellcheck.entities.BaneoEntity;
import com.agg.dumbellcheck.entities.NotificacionEntity;
import com.agg.dumbellcheck.entities.SeguidorEntity;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Single file containing all mappers related to User Identity and Social Domain.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserInfoMapper {
    @BeanMapping(ignoreUnmappedSourceProperties = "password")
    UsuarioDto toUsuarioDto(UsuarioEntity entity);

    @Mapping(target = "usuarioId", source = "usuario.id")
    BaneoDto toBaneoDto(BaneoEntity entity);

    @Mapping(target = "usuario.id", source = "usuarioId")
    BaneoEntity toBaneoEntity(BaneoDto dto);

    @Mapping(target = "rutinas", ignore = true)
    @Mapping(target = "publicaciones", ignore = true)
    @Mapping(target = "comentarios", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "incidencias", ignore = true)
    @Mapping(target = "notificaciones", ignore = true)
    @Mapping(target = "chatsCreados", ignore = true)
    @Mapping(target = "mensajesChat", ignore = true)
    @Mapping(target = "chatsUsuario", ignore = true)
    @Mapping(target = "siguiendo", ignore = true)
    @Mapping(target = "seguidores", ignore = true)
    @Mapping(target = "baneos", ignore = true)
    @Mapping(target = "password", ignore = true)
    UsuarioEntity toUsuarioEntity(UsuarioDto dto);

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "seguidoId", source = "seguido.id")
    SeguidorDto toSeguidorDto(SeguidorEntity entity);

    @Mapping(target = "usuario.id", source = "usuarioId")
    @Mapping(target = "seguido.id", source = "seguidoId")
    SeguidorEntity toSeguidorEntity(SeguidorDto dto);

    @Mapping(target = "usuarioId", source = "usuario.id")
    NotificacionDto toNotificacionDto(NotificacionEntity entity);

    @Mapping(target = "usuario.id", source = "usuarioId")
    NotificacionEntity toNotificacionEntity(NotificacionDto dto);
}