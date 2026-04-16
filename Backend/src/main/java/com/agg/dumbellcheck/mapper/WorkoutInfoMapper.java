package com.agg.dumbellcheck.mapper;

/*
ejercicios (Catálogo maestro de ejercicios).

rutinas (La cabecera de la rutina).

ejercicios_rutina (La composición de la plantilla).
*/

import com.agg.dumbellcheck.dto.WorkoutInfoDTO.*;
import com.agg.dumbellcheck.entities.EjercicioEntity;
import com.agg.dumbellcheck.entities.EjercicioRutinaEntity;
import com.agg.dumbellcheck.entities.RutinaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkoutInfoMapper {
    EjercicioDto toEjercicioDto(EjercicioEntity entity);

    @Mapping(target = "rutinas", ignore = true)
    @Mapping(target = "publicaciones", ignore = true)
    EjercicioEntity toEjercicioEntity(EjercicioDto dto);

    @Mapping(target = "usuarioId", source = "usuario.id")
    RutinaDto toRutinaDto(RutinaEntity entity);

    @Mapping(target = "usuario.id", source = "usuarioId")
    @Mapping(target = "ejercicios", ignore = true)
    @Mapping(target = "publicacionesOrigen", ignore = true)
    @Mapping(target = "mensajesCompartidos", ignore = true)
    RutinaEntity toRutinaEntity(RutinaDto dto);

    @Mapping(target = "rutinaId", source = "rutina.id")
    @Mapping(target = "ejercicioId", source = "ejercicio.id")
    EjercicioRutinaDto toEjercicioRutinaDto(EjercicioRutinaEntity entity);

    @Mapping(target = "rutina.id", source = "rutinaId")
    @Mapping(target = "ejercicio.id", source = "ejercicioId")
    EjercicioRutinaEntity toEjercicioRutinaEntity(EjercicioRutinaDto dto);
}
