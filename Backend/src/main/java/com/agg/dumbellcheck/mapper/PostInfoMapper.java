package com.agg.dumbellcheck.mapper;

/*
publicaciones (El post del entrenamiento).

ejercicios_publicacion (Ejercicios realizados en ese post).

detalles_series (Las series, pesos y repeticiones reales).

comentarios (Interacción en el post).

likes (Reacciones).
*/

import com.agg.dumbellcheck.dto.PostInfoDTO.*;
import com.agg.dumbellcheck.entities.ComentarioEntity;
import com.agg.dumbellcheck.entities.DetalleSerieEntity;
import com.agg.dumbellcheck.entities.EjercicioPublicacionEntity;
import com.agg.dumbellcheck.entities.LikeEntity;
import com.agg.dumbellcheck.entities.PublicacionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostInfoMapper {
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "rutinaOrigenId", source = "rutinaOrigen.id")
    PublicacionDto toPublicacionDto(PublicacionEntity entity);

    @Mapping(target = "usuario.id", source = "usuarioId")
    @Mapping(target = "rutinaOrigen.id", source = "rutinaOrigenId")
    @Mapping(target = "ejercicios", ignore = true)
    @Mapping(target = "comentarios", ignore = true)
    PublicacionEntity toPublicacionEntity(PublicacionDto dto);

    @Mapping(target = "publicacionId", source = "publicacion.id")
    @Mapping(target = "ejercicioId", source = "ejercicio.id")
    EjercicioPublicacionDto toEjercicioPublicacionDto(EjercicioPublicacionEntity entity);

    @Mapping(target = "publicacion.id", source = "publicacionId")
    @Mapping(target = "ejercicio.id", source = "ejercicioId")
    @Mapping(target = "detallesSeries", ignore = true)
    EjercicioPublicacionEntity toEjercicioPublicacionEntity(EjercicioPublicacionDto dto);

    @Mapping(target = "ejercicioPublicacionId", source = "ejercicioPublicacion.id")
    DetalleSerieDto toDetalleSerieDto(DetalleSerieEntity entity);

    @Mapping(target = "ejercicioPublicacion.id", source = "ejercicioPublicacionId")
    DetalleSerieEntity toDetalleSerieEntity(DetalleSerieDto dto);

    @Mapping(target = "publicacionId", source = "publicacion.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "comentarioPadreId", source = "comentarioPadre.id")
    ComentarioDto toComentarioDto(ComentarioEntity entity);

    @Mapping(target = "publicacion.id", source = "publicacionId")
    @Mapping(target = "usuario.id", source = "usuarioId")
    @Mapping(target = "comentarioPadre.id", source = "comentarioPadreId")
    @Mapping(target = "respuestas", ignore = true)
    ComentarioEntity toComentarioEntity(ComentarioDto dto);

    @Mapping(target = "usuarioId", source = "usuario.id")
    LikeDto toLikeDto(LikeEntity entity);

    @Mapping(target = "usuario.id", source = "usuarioId")
    LikeEntity toLikeEntity(LikeDto dto);
}
