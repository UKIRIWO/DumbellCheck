package com.agg.dumbellcheck.mapper;

/*
chats (Cabeceras de chat/grupo).

usuarios_chat (Participantes y roles).

mensajes_chat (El contenido de los mensajes).
*/

import com.agg.dumbellcheck.dto.MessageInfoDTO.*;
import com.agg.dumbellcheck.entities.ChatEntity;
import com.agg.dumbellcheck.entities.MensajeChatEntity;
import com.agg.dumbellcheck.entities.UsuarioChatEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageInfoMapper {
    @Mapping(target = "creadorId", source = "creador.id")
    @Mapping(target = "ultimoMensajeId", source = "ultimoMensaje.id")
    ChatDto toChatDto(ChatEntity entity);

    @Mapping(target = "creador.id", source = "creadorId")
    @Mapping(target = "ultimoMensaje.id", source = "ultimoMensajeId")
    @Mapping(target = "incidencias", ignore = true)
    @Mapping(target = "mensajes", ignore = true)
    @Mapping(target = "participantes", ignore = true)
    ChatEntity toChatEntity(ChatDto dto);

    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    UsuarioChatDto toUsuarioChatDto(UsuarioChatEntity entity);

    @Mapping(target = "chat.id", source = "chatId")
    @Mapping(target = "usuario.id", source = "usuarioId")
    UsuarioChatEntity toUsuarioChatEntity(UsuarioChatDto dto);

    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "rutinaId", source = "rutina.id")
    @Mapping(target = "mensajeReferenciaId", source = "mensajeReferencia.id")
    MensajeChatDto toMensajeChatDto(MensajeChatEntity entity);

    @Mapping(target = "chat.id", source = "chatId")
    @Mapping(target = "usuario.id", source = "usuarioId")
    @Mapping(target = "rutina.id", source = "rutinaId")
    @Mapping(target = "mensajeReferencia.id", source = "mensajeReferenciaId")
    MensajeChatEntity toMensajeChatEntity(MensajeChatDto dto);
}
