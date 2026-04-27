package com.agg.dumbellcheck.mapper;

import com.agg.dumbellcheck.dto.WorkoutInfoDTO.DetalleSerieRutinaDto;
import com.agg.dumbellcheck.dto.WorkoutInfoDTO.EjercicioDto;
import com.agg.dumbellcheck.dto.WorkoutInfoDTO.EjercicioGrupoMuscularDto;
import com.agg.dumbellcheck.dto.WorkoutInfoDTO.EjercicioRutinaDto;
import com.agg.dumbellcheck.dto.WorkoutInfoDTO.GrupoMuscularDto;
import com.agg.dumbellcheck.dto.WorkoutInfoDTO.RutinaDto;
import com.agg.dumbellcheck.entities.DetalleSerieRutinaEntity;
import com.agg.dumbellcheck.entities.EjercicioEntity;
import com.agg.dumbellcheck.entities.EjercicioGrupoMuscularEntity;
import com.agg.dumbellcheck.entities.EjercicioGrupoMuscularId;
import com.agg.dumbellcheck.entities.EjercicioRutinaEntity;
import com.agg.dumbellcheck.entities.GrupoMuscularEntity;
import com.agg.dumbellcheck.entities.RutinaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkoutInfoMapper {

    EjercicioDto toEjercicioDto(EjercicioEntity entity);

    @Mapping(target = "rutinas", ignore = true)
    @Mapping(target = "publicaciones", ignore = true)
    @Mapping(target = "gruposMusculares", ignore = true)
    EjercicioEntity toEjercicioEntity(EjercicioDto dto);

    GrupoMuscularDto toGrupoMuscularDto(GrupoMuscularEntity entity);

    @Mapping(target = "ejercicios", ignore = true)
    GrupoMuscularEntity toGrupoMuscularEntity(GrupoMuscularDto dto);

    @Mapping(target = "ejercicioId", source = "id.ejercicioId")
    @Mapping(target = "grupoMuscularId", source = "id.grupoMuscularId")
    EjercicioGrupoMuscularDto toEjercicioGrupoMuscularDto(EjercicioGrupoMuscularEntity entity);

    @Mapping(target = "id", source = "dto")
    @Mapping(target = "ejercicio.id", source = "ejercicioId")
    @Mapping(target = "grupoMuscular.id", source = "grupoMuscularId")
    EjercicioGrupoMuscularEntity toEjercicioGrupoMuscularEntity(EjercicioGrupoMuscularDto dto);

    default EjercicioGrupoMuscularId toId(EjercicioGrupoMuscularDto dto) {
        EjercicioGrupoMuscularId id = new EjercicioGrupoMuscularId();
        id.setEjercicioId(dto.ejercicioId());
        id.setGrupoMuscularId(dto.grupoMuscularId());
        return id;
    }

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
    @Mapping(target = "detallesSeries", ignore = true)
    EjercicioRutinaEntity toEjercicioRutinaEntity(EjercicioRutinaDto dto);

    @Mapping(target = "ejercicioRutinaId", source = "ejercicioRutina.id")
    DetalleSerieRutinaDto toDetalleSerieRutinaDto(DetalleSerieRutinaEntity entity);

    @Mapping(target = "ejercicioRutina.id", source = "ejercicioRutinaId")
    DetalleSerieRutinaEntity toDetalleSerieRutinaEntity(DetalleSerieRutinaDto dto);
}
