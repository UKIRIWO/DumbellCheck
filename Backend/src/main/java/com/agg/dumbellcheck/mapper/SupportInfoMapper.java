package com.agg.dumbellcheck.mapper;

/*
incidencias (Tickets de soporte o reportes).
*/

import com.agg.dumbellcheck.dto.SupportInfoDTO.IncidenciaDto;
import com.agg.dumbellcheck.entities.IncidenciaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupportInfoMapper {
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "chatId", source = "chat.id")
    IncidenciaDto toIncidenciaDto(IncidenciaEntity entity);

    @Mapping(target = "usuario.id", source = "usuarioId")
    @Mapping(target = "chat.id", source = "chatId")
    IncidenciaEntity toIncidenciaEntity(IncidenciaDto dto);
}
