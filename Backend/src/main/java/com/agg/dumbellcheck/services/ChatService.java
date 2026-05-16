package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.*;
import com.agg.dumbellcheck.entities.*;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.exceptions.UnauthorizedActionException;
import com.agg.dumbellcheck.repositories.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final int MAX_GROUP_MEMBERS = 10;

    private final ChatRepository chatRepository;
    private final UsuarioChatRepository usuarioChatRepository;
    private final MensajeChatRepository mensajeChatRepository;
    private final UsuarioRepository usuarioRepository;
    private final RutinaRepository rutinaRepository;
    private final MediaStorageService mediaStorageService;

    public ChatService(
            ChatRepository chatRepository,
            UsuarioChatRepository usuarioChatRepository,
            MensajeChatRepository mensajeChatRepository,
            UsuarioRepository usuarioRepository,
            RutinaRepository rutinaRepository,
            MediaStorageService mediaStorageService) {
        this.chatRepository = chatRepository;
        this.usuarioChatRepository = usuarioChatRepository;
        this.mensajeChatRepository = mensajeChatRepository;
        this.usuarioRepository = usuarioRepository;
        this.rutinaRepository = rutinaRepository;
        this.mediaStorageService = mediaStorageService;
    }

    // ── Chat list ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ChatListItemResponse> getMisChats(String username) {
        UsuarioEntity me = findUser(username);
        List<ChatEntity> chats = chatRepository.findChatsForUser(me.getId());

        return chats.stream().map(chat -> buildListItem(chat, me)).toList();
    }

    // ── Create direct chat (find-or-create) ───────────────────────────────

    @Transactional
    public ChatDetailResponse findOrCreateDirectChat(String username, String targetUsername) {
        UsuarioEntity me = findUser(username);
        UsuarioEntity target = findUser(targetUsername);

        Optional<ChatEntity> existing = chatRepository.findDirectChatBetween(me.getId(), target.getId());
        if (existing.isPresent()) {
            return buildDetailResponse(existing.get(), me);
        }

        ChatEntity chat = new ChatEntity();
        chat.setTipo(TipoChat.directo);
        chat.setCreador(me);
        chat.setFechaCreacion(Instant.now());
        chat.setFechaUltimaActividad(Instant.now());
        ChatEntity saved = chatRepository.save(chat);

        addParticipant(saved, me, RolChatUsuario.admin);
        addParticipant(saved, target, RolChatUsuario.miembro);

        return buildDetailResponse(saved, me);
    }

    // ── Create group chat ─────────────────────────────────────────────────

    @Transactional
    public ChatDetailResponse createGroupChat(String username, CreateGroupChatRequest request) {
        UsuarioEntity me = findUser(username);

        ChatEntity chat = new ChatEntity();
        chat.setTipo(TipoChat.grupo);
        chat.setNombre(request.nombre() != null && !request.nombre().isBlank()
                ? request.nombre().trim() : null);
        chat.setCreador(me);
        chat.setFechaCreacion(Instant.now());
        chat.setFechaUltimaActividad(Instant.now());
        ChatEntity saved = chatRepository.save(chat);

        addParticipant(saved, me, RolChatUsuario.admin);

        Set<String> seen = new java.util.HashSet<>();
        seen.add(username.toLowerCase());
        for (String uname : request.usernames()) {
            if (seen.contains(uname.toLowerCase())) continue;
            seen.add(uname.toLowerCase());
            usuarioRepository.findByUsername(uname).ifPresent(u -> addParticipant(saved, u, RolChatUsuario.miembro));
        }

        return buildDetailResponse(saved, me);
    }

    @Transactional
    public ChatDetailResponse addGroupMembers(String publicId, String username, AddGroupMembersRequest request) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);
        requireChatAdmin(chat.getId(), me);

        if (chat.getTipo() != TipoChat.grupo) {
            throw new UnauthorizedActionException("Solo puedes añadir miembros en chats de grupo");
        }

        List<UsuarioChatEntity> participantes = usuarioChatRepository.findByChatId(chat.getId());
        java.util.Set<Integer> memberIds = participantes.stream()
                .map(p -> p.getUsuario().getId())
                .collect(java.util.stream.Collectors.toSet());

        int added = 0;
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String uname : request.usernames()) {
            if (uname == null || uname.isBlank()) continue;
            String key = uname.trim().toLowerCase();
            if (seen.contains(key)) continue;
            seen.add(key);

            UsuarioEntity user = usuarioRepository.findByUsername(uname.trim()).orElse(null);
            if (user == null || memberIds.contains(user.getId())) continue;

            if (participantes.size() + added >= MAX_GROUP_MEMBERS) {
                throw new UnauthorizedActionException(
                        "El grupo no puede tener más de " + MAX_GROUP_MEMBERS + " miembros");
            }

            addParticipant(chat, user, RolChatUsuario.miembro);
            memberIds.add(user.getId());
            added++;
        }

        return buildDetailResponse(chat, me);
    }

    // ── Chat detail ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ChatDetailResponse getChatDetail(String publicId, String username) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);
        return buildDetailResponse(chat, me);
    }

    // ── Messages ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CursorPageResponse<ChatMessageResponse> getMensajes(
            String publicId, String username, Integer cursor, int size) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);

        int pageSize = Math.min(size, 50);
        List<MensajeChatEntity> rows = mensajeChatRepository.findByChatBeforeCursor(
                chat.getId(), cursor, PageRequest.of(0, pageSize + 1));

        boolean hasMore = rows.size() > pageSize;
        List<MensajeChatEntity> page = hasMore ? rows.subList(0, pageSize) : rows;
        Integer nextCursor = hasMore ? page.get(page.size() - 1).getId() : null;

        // Return in ascending order (oldest first for rendering)
        List<ChatMessageResponse> content = page.stream()
                .sorted((a, b) -> a.getId() - b.getId())
                .map(m -> toMessageResponse(m, me))
                .toList();

        return new CursorPageResponse<>(content, nextCursor, hasMore);
    }

    @Transactional
    public ChatMessageResponse sendMessage(String publicId, String username, SendMessageRequest request) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);

        TipoMensajeChat tipo = TipoMensajeChat.valueOf(request.tipoMensaje().toLowerCase());

        MensajeChatEntity msg = new MensajeChatEntity();
        msg.setChat(chat);
        msg.setUsuario(me);
        msg.setTipoMensaje(tipo);
        msg.setContenido(request.contenido());
        msg.setArchivoUrl(request.archivoUrl());
        msg.setEstaEditado(false);
        msg.setFechaCreacion(Instant.now());

        if (request.rutinaId() != null) {
            rutinaRepository.findById(request.rutinaId()).ifPresent(msg::setRutina);
        }
        if (request.mensajeReferenciaId() != null) {
            mensajeChatRepository.findByChatIdAndId(chat.getId(), request.mensajeReferenciaId())
                    .ifPresent(msg::setMensajeReferencia);
        }

        MensajeChatEntity saved = mensajeChatRepository.save(msg);

        chat.setUltimoMensaje(saved);
        chat.setFechaUltimaActividad(Instant.now());
        chatRepository.save(chat);

        return toMessageResponse(saved, me);
    }

    @Transactional
    public ChatMessageResponse editMessage(String publicId, Integer mensajeId,
                                           String username, UpdateMessageRequest request) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);
        MensajeChatEntity msg = findMessage(chat.getId(), mensajeId);

        if (!msg.getUsuario().getId().equals(me.getId())) {
            throw new UnauthorizedActionException("No puedes editar este mensaje");
        }
        if (msg.getEliminadoEn() != null) {
            throw new UnauthorizedActionException("No puedes editar un mensaje eliminado");
        }

        msg.setContenido(request.contenido());
        msg.setEstaEditado(true);
        msg.setFechaEdicion(Instant.now());

        return toMessageResponse(mensajeChatRepository.save(msg), me);
    }

    @Transactional
    public void deleteMessage(String publicId, Integer mensajeId, String username) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);
        MensajeChatEntity msg = findMessage(chat.getId(), mensajeId);

        boolean isOwner = msg.getUsuario().getId().equals(me.getId());
        boolean isChatAdmin = usuarioChatRepository.findByChat_IdAndUsuario_Id(chat.getId(), me.getId())
                .map(uc -> uc.getRol() == RolChatUsuario.admin)
                .orElse(false);

        if (!isOwner && !isChatAdmin) {
            throw new UnauthorizedActionException("No puedes eliminar este mensaje");
        }

        msg.setEliminadoEn(Instant.now());
        msg.setContenido(null);
        msg.setArchivoUrl(null);
        mensajeChatRepository.save(msg);
    }

    @Transactional
    public void updateLastSeen(String publicId, String username) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);
        usuarioChatRepository.updateLastSeen(chat.getId(), me.getId(), Instant.now());
    }

    @Transactional
    public ChatDetailResponse updateChatNombre(String publicId, String username, UpdateChatRequest request) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);
        requireChatAdmin(chat.getId(), me);

        if (chat.getTipo() != TipoChat.grupo) {
            throw new UnauthorizedActionException("Solo los chats de grupo pueden cambiar de nombre");
        }

        String nombre = request.nombre() != null ? request.nombre().trim() : "";
        chat.setNombre(nombre.isBlank() ? null : nombre);
        chatRepository.save(chat);

        return buildDetailResponse(chat, me);
    }

    @Transactional
    public ChatDetailResponse uploadGroupPhoto(String publicId, String username,
                                               org.springframework.web.multipart.MultipartFile file) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);
        requireChatAdmin(chat.getId(), me);

        if (chat.getTipo() != TipoChat.grupo) {
            throw new UnauthorizedActionException("Solo los chats de grupo pueden tener foto");
        }

        String url = mediaStorageService.storeGroupPhoto(file);
        chat.setFotoGrupoUrl(url);
        chatRepository.save(chat);

        return buildDetailResponse(chat, me);
    }

    @Transactional
    public void leaveChat(String publicId, String username) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        if (chat.getTipo() == TipoChat.soporte) {
            throw new UnauthorizedActionException("No puedes abandonar un chat de soporte");
        }

        if (chat.getTipo() == TipoChat.grupo) {
            promoteNextAdminIfSoleAdminLeaving(chat.getId(), me.getId());
        }

        int deleted = usuarioChatRepository.deleteMembership(chat.getId(), me.getId());
        if (deleted == 0) {
            throw new ResourceNotFoundException("No eres miembro de este chat");
        }
    }

    @Transactional
    public ChatDetailResponse updateParticipantRole(
            String publicId, Integer usuarioId, String username, UpdateParticipantRoleRequest request) {
        UsuarioEntity me = findUser(username);
        ChatEntity chat = findChatByPublicId(publicId);
        requireMember(chat, me);
        requireChatAdmin(chat.getId(), me);

        UsuarioChatEntity membership = usuarioChatRepository.findByChat_IdAndUsuario_Id(chat.getId(), usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Participante no encontrado en el chat"));

        RolChatUsuario nuevoRol = RolChatUsuario.valueOf(request.rol());
        if (membership.getRol() == nuevoRol) {
            return buildDetailResponse(chat, me);
        }

        if (membership.getUsuario().getId().equals(me.getId()) && nuevoRol == RolChatUsuario.miembro) {
            long admins = usuarioChatRepository.findByChatId(chat.getId()).stream()
                    .filter(p -> p.getRol() == RolChatUsuario.admin)
                    .count();
            if (admins <= 1) {
                throw new UnauthorizedActionException("Debe haber al menos un administrador en el chat");
            }
        }

        membership.setRol(nuevoRol);
        usuarioChatRepository.save(membership);

        return buildDetailResponse(chat, me);
    }

    // ── User search ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ChatSearchUserResponse> searchUsers(String username, String q, int page, int size) {
        UsuarioEntity me = findUser(username);
        String search = (q != null && !q.isBlank()) ? q.trim() : null;
        int pageSize = Math.min(size, 50);
        long offset = (long) page * pageSize;

        List<UsuarioEntity> users = usuarioRepository.searchUsersForChat(me.getId(), search, pageSize, offset);

        Set<Integer> following = me.getSiguiendo().stream()
                .map(s -> s.getSeguido().getId())
                .collect(Collectors.toSet());

        return users.stream().map(u -> new ChatSearchUserResponse(
                u.getId(),
                u.getUsername(),
                u.getNombre(),
                u.getFotoPerfilUrl(),
                following.contains(u.getId())
        )).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Si el usuario que sale es el único admin, asciende al primer miembro restante
     * (orden de la lista: id ascendente, el de más arriba en el modal).
     */
    private void promoteNextAdminIfSoleAdminLeaving(Integer chatId, Integer leavingUserId) {
        List<UsuarioChatEntity> participantes = usuarioChatRepository.findByChatIdOrderByIdAsc(chatId);

        boolean leavingIsAdmin = participantes.stream()
                .anyMatch(p -> p.getUsuario().getId().equals(leavingUserId)
                        && p.getRol() == RolChatUsuario.admin);
        if (!leavingIsAdmin) {
            return;
        }

        long adminCount = participantes.stream()
                .filter(p -> p.getRol() == RolChatUsuario.admin)
                .count();
        if (adminCount > 1) {
            return;
        }

        participantes.stream()
                .filter(p -> !p.getUsuario().getId().equals(leavingUserId))
                .findFirst()
                .ifPresent(next -> {
                    next.setRol(RolChatUsuario.admin);
                    usuarioChatRepository.save(next);
                });
    }

    private void addParticipant(ChatEntity chat, UsuarioEntity user, RolChatUsuario rol) {
        UsuarioChatEntity uc = new UsuarioChatEntity();
        uc.setChat(chat);
        uc.setUsuario(user);
        uc.setRol(rol);
        uc.setFechaUnion(Instant.now());
        uc.setNotificacionesActivas(true);
        usuarioChatRepository.save(uc);
    }

    private ChatListItemResponse buildListItem(ChatEntity chat, UsuarioEntity me) {
        List<UsuarioChatEntity> participantes = usuarioChatRepository.findByChatId(chat.getId());
        boolean esGrupo = chat.getTipo() == TipoChat.grupo;

        String nombre = esGrupo
                ? (chat.getNombre() != null ? chat.getNombre() : buildGroupName(participantes, me))
                : getOtherUsername(participantes, me);

        String fotoUrl = esGrupo
                ? chat.getFotoGrupoUrl()
                : getOtherFotoUrl(participantes, me);

        ChatListItemResponse.UltimoMensajePreview preview = null;
        List<MensajeChatEntity> latest = mensajeChatRepository.findLatestByChatId(
                chat.getId(), PageRequest.of(0, 1));
        if (!latest.isEmpty()) {
            MensajeChatEntity last = latest.get(0);
            preview = new ChatListItemResponse.UltimoMensajePreview(
                    last.getTipoMensaje().name(),
                    buildPreviewText(last),
                    last.getUsuario().getUsername(),
                    last.getUsuario().getId().equals(me.getId()),
                    last.getFechaCreacion()
            );
        }

        long unread = mensajeChatRepository.countUnread(chat.getId(), me.getId());

        List<ChatListItemResponse.ParticipanteResumen> participantesDtos = participantes.stream()
                .map(p -> new ChatListItemResponse.ParticipanteResumen(
                        p.getUsuario().getId(),
                        p.getUsuario().getUsername(),
                        p.getUsuario().getFotoPerfilUrl()))
                .toList();

        return new ChatListItemResponse(
                chat.getPublicId(),
                nombre,
                chat.getTipo().name(),
                fotoUrl,
                preview,
                (int) unread,
                chat.getFechaUltimaActividad(),
                participantesDtos
        );
    }

    private ChatDetailResponse buildDetailResponse(ChatEntity chat, UsuarioEntity me) {
        List<UsuarioChatEntity> participantes = usuarioChatRepository.findByChatIdOrderByIdAsc(chat.getId());
        boolean esGrupo = chat.getTipo() == TipoChat.grupo;

        String nombre = esGrupo
                ? (chat.getNombre() != null ? chat.getNombre() : buildGroupName(participantes, me))
                : getOtherUsername(participantes, me);

        String fotoUrl = esGrupo
                ? chat.getFotoGrupoUrl()
                : getOtherFotoUrl(participantes, me);

        boolean soyAdmin = participantes.stream()
                .anyMatch(p -> p.getUsuario().getId().equals(me.getId()) && p.getRol() == RolChatUsuario.admin);

        List<ChatDetailResponse.ParticipanteDto> parts = participantes.stream()
                .map(p -> new ChatDetailResponse.ParticipanteDto(
                        p.getUsuario().getId(),
                        p.getUsuario().getUsername(),
                        p.getUsuario().getNombre(),
                        p.getUsuario().getFotoPerfilUrl(),
                        p.getRol().name()))
                .toList();

        return new ChatDetailResponse(chat.getPublicId(), nombre, chat.getTipo().name(),
                fotoUrl, esGrupo, soyAdmin, parts);
    }

    private ChatMessageResponse toMessageResponse(MensajeChatEntity m, UsuarioEntity me) {
        boolean eliminado = m.getEliminadoEn() != null;
        String rutinaPublicId = m.getRutina() != null ? m.getRutina().getPublicId() : null;
        String rutinaNombre = m.getRutina() != null ? m.getRutina().getNombre() : null;

        String refPreview = null;
        String refUsername = null;
        if (m.getMensajeReferencia() != null) {
            MensajeChatEntity ref = m.getMensajeReferencia();
            refUsername = ref.getUsuario().getUsername();
            refPreview = ref.getEliminadoEn() != null ? "Mensaje eliminado"
                    : ref.getTipoMensaje() == TipoMensajeChat.texto
                    ? (ref.getContenido() != null && ref.getContenido().length() > 60
                    ? ref.getContenido().substring(0, 60) + "…"
                    : ref.getContenido())
                    : ref.getTipoMensaje().name();
        }

        return new ChatMessageResponse(
                m.getId(),
                m.getChat().getPublicId(),
                m.getUsuario().getId(),
                m.getUsuario().getUsername(),
                m.getUsuario().getFotoPerfilUrl(),
                m.getTipoMensaje().name(),
                eliminado ? null : m.getContenido(),
                eliminado ? null : m.getArchivoUrl(),
                m.getRutina() != null ? m.getRutina().getId() : null,
                rutinaNombre,
                rutinaPublicId,
                m.getMensajeReferencia() != null ? m.getMensajeReferencia().getId() : null,
                refPreview,
                refUsername,
                m.isEstaEditado(),
                eliminado,
                m.getUsuario().getId().equals(me.getId()),
                m.getFechaCreacion(),
                m.getFechaEdicion()
        );
    }

    private String buildGroupName(List<UsuarioChatEntity> participantes, UsuarioEntity me) {
        return participantes.stream()
                .filter(p -> !p.getUsuario().getId().equals(me.getId()))
                .limit(3)
                .map(p -> p.getUsuario().getUsername())
                .collect(Collectors.joining(", "));
    }

    private String getOtherUsername(List<UsuarioChatEntity> participantes, UsuarioEntity me) {
        return participantes.stream()
                .filter(p -> !p.getUsuario().getId().equals(me.getId()))
                .findFirst()
                .map(p -> p.getUsuario().getUsername())
                .orElse("Desconocido");
    }

    private String getOtherFotoUrl(List<UsuarioChatEntity> participantes, UsuarioEntity me) {
        return participantes.stream()
                .filter(p -> !p.getUsuario().getId().equals(me.getId()))
                .findFirst()
                .map(p -> p.getUsuario().getFotoPerfilUrl())
                .orElse(null);
    }

    private UsuarioEntity findUser(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private ChatEntity findChatByPublicId(String publicId) {
        return chatRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat no encontrado"));
    }

    private MensajeChatEntity findMessage(Integer chatId, Integer mensajeId) {
        return mensajeChatRepository.findByChatIdAndId(chatId, mensajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado: " + mensajeId));
    }

    private void requireMember(ChatEntity chat, UsuarioEntity user) {
        if (!chatRepository.isMember(chat.getId(), user.getId())) {
            throw new UnauthorizedActionException("No eres miembro de este chat");
        }
    }

    private void requireChatAdmin(Integer chatId, UsuarioEntity user) {
        boolean isAdmin = usuarioChatRepository.findByChat_IdAndUsuario_Id(chatId, user.getId())
                .map(uc -> uc.getRol() == RolChatUsuario.admin)
                .orElse(false);
        if (!isAdmin) {
            throw new UnauthorizedActionException("Solo los administradores pueden realizar esta acción");
        }
    }

    private String buildPreviewText(MensajeChatEntity message) {
        if (message.getEliminadoEn() != null) {
            return null;
        }
        return switch (message.getTipoMensaje()) {
            case texto -> message.getContenido();
            case imagen -> "Imagen";
            case video -> "Vídeo";
            case rutina -> "Rutina";
        };
    }
}
