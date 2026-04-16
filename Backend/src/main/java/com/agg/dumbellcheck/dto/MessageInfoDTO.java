package com.agg.dumbellcheck.dto;

import java.time.Instant;
import com.agg.dumbellcheck.entities.RolChatUsuario;
import com.agg.dumbellcheck.entities.TipoMensajeChat;

public final class MessageInfoDTO {

    private MessageInfoDTO() {}

    public record ChatDto(
        Integer id,
        String nombre,
        String tipo,
        Integer creadorId,
        String fotoGrupoUrl,
        Integer ultimoMensajeId,
        Instant fechaCreacion,
        Instant fechaUltimaActividad
    ) {}

    public record UsuarioChatDto(
        Integer id,
        Integer chatId,
        Integer usuarioId,
        RolChatUsuario rol,
        Instant fechaUnion,
        Instant fechaUltimaVista,
        boolean notificacionesActivas
    ) {}

    public record MensajeChatDto(
        Integer id,
        Integer chatId,
        Integer usuarioId,
        TipoMensajeChat tipoMensaje,
        String contenido,
        String archivoUrl,
        Integer rutinaId,
        Integer mensajeReferenciaId,
        boolean estaEditado,
        Instant eliminadoEn,
        Instant fechaCreacion,
        Instant fechaEdicion
    ) {}
}
