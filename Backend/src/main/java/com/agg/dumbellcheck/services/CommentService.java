package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.CommentCreateRequest;
import com.agg.dumbellcheck.dto.CommentResponse;
import com.agg.dumbellcheck.entities.ComentarioEntity;
import com.agg.dumbellcheck.entities.PublicacionEntity;
import com.agg.dumbellcheck.entities.TipoLike;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.exceptions.UnauthorizedActionException;
import com.agg.dumbellcheck.repositories.ComentarioRepository;
import com.agg.dumbellcheck.repositories.LikeRepository;
import com.agg.dumbellcheck.repositories.PublicacionRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9_]{1,50})");

    private final ComentarioRepository comentarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final LikeRepository likeRepository;

    public CommentService(
            ComentarioRepository comentarioRepository,
            PublicacionRepository publicacionRepository,
            UsuarioRepository usuarioRepository,
            LikeRepository likeRepository) {
        this.comentarioRepository = comentarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPublicId(String publicId, String currentUsername) {
        PublicacionEntity publicacion = publicacionRepository.findByPublicIdAndEstaActivaTrue(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        UsuarioEntity currentUser = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<ComentarioEntity> comments = comentarioRepository.findAllByPublicacionIdWithUsuario(publicacion.getId());
        Map<Integer, ComentarioEntity> rootEntities = new LinkedHashMap<>();
        Map<Integer, List<ComentarioEntity>> activeRepliesByRootId = new LinkedHashMap<>();

        for (ComentarioEntity comment : comments) {
            ComentarioEntity parent = comment.getComentarioPadre();
            if (parent == null) {
                rootEntities.put(comment.getId(), comment);
                activeRepliesByRootId.putIfAbsent(comment.getId(), new ArrayList<>());
            } else if (comment.isEstaActivo()) {
                Integer rootId = parent.getComentarioPadre() == null
                        ? parent.getId()
                        : parent.getComentarioPadre().getId();
                activeRepliesByRootId.computeIfAbsent(rootId, ignored -> new ArrayList<>()).add(comment);
            }
        }

        Map<String, String> validMentions = resolveValidMentions(collectAllMentions(comments));
        Set<Integer> likedCommentIds = likedCommentIds(currentUser.getId(), comments);

        return rootEntities.values().stream()
                .filter(root -> {
                    List<ComentarioEntity> replies = activeRepliesByRootId.getOrDefault(root.getId(), Collections.emptyList());
                    return root.isEstaActivo() || !replies.isEmpty();
                })
                .map(root -> toResponse(
                        root,
                        activeRepliesByRootId.getOrDefault(root.getId(), Collections.emptyList()),
                        validMentions,
                        likedCommentIds))
                .toList();
    }

    @Transactional
    public void deleteComment(String publicId, Integer commentId, String username) {
        PublicacionEntity publicacion = publicacionRepository.findByPublicIdAndEstaActivaTrue(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        ComentarioEntity comentario = comentarioRepository
                .findByIdAndPublicacionIdAndEstaActivoTrue(commentId, publicacion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));

        if (!comentario.getUsuario().getUsername().equals(username)) {
            throw new UnauthorizedActionException("No puedes eliminar este comentario");
        }

        boolean isRoot = comentario.getComentarioPadre() == null;
        boolean hasActiveReplies = isRoot
                && comentarioRepository.existsByComentarioPadreIdAndEstaActivoTrue(comentario.getId());

        if (hasActiveReplies) {
            // Soft delete to preserve the thread.
            // Triggers only fire on INSERT/DELETE, so the publication counter must be adjusted manually here.
            comentario.setEstaActivo(false);
            comentarioRepository.save(comentario);

            Integer current = publicacion.getContadorComentarios() == null ? 0 : publicacion.getContadorComentarios();
            publicacion.setContadorComentarios(Math.max(0, current - 1));
            publicacionRepository.save(publicacion);
        } else {
            // Hard delete; the after_delete trigger keeps the publication counter in sync.
            comentarioRepository.delete(comentario);
        }
    }

    @Transactional
    public CommentResponse createComment(String publicId, String username, CommentCreateRequest request) {
        PublicacionEntity publicacion = publicacionRepository.findByPublicIdAndEstaActivaTrue(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ComentarioEntity comentario = new ComentarioEntity();
        comentario.setPublicacion(publicacion);
        comentario.setUsuario(usuario);
        comentario.setTexto(request.texto().trim());
        if (request.comentarioPadreId() != null) {
            ComentarioEntity requestedParent = comentarioRepository
                    .findByIdAndPublicacionIdAndEstaActivoTrue(request.comentarioPadreId(), publicacion.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentario padre no encontrado"));
            comentario.setComentarioPadre(resolveRootComment(requestedParent));
        }
        comentario.setContadorLikes(0);
        comentario.setEstaActivo(true);
        comentario.setFechaCreacion(Instant.now());

        comentarioRepository.save(comentario);
        Map<String, String> validMentions = resolveValidMentions(extractMentionsLowercase(comentario.getTexto()));
        return toResponse(comentario, Collections.emptyList(), validMentions, Collections.emptySet());
    }

    private ComentarioEntity resolveRootComment(ComentarioEntity comment) {
        ComentarioEntity root = comment;
        while (root.getComentarioPadre() != null) {
            root = root.getComentarioPadre();
        }
        return root;
    }

    private CommentResponse toResponse(
            ComentarioEntity c,
            List<ComentarioEntity> replies,
            Map<String, String> validMentions,
            Set<Integer> likedCommentIds) {
        boolean eliminado = !c.isEstaActivo();
        UsuarioEntity u = c.getUsuario();
        CommentResponse.UsuarioResumen usuarioDto = eliminado
                ? null
                : new CommentResponse.UsuarioResumen(u.getId(), u.getUsername(), u.getFotoPerfilUrl());
        String texto = eliminado ? null : c.getTexto();
        List<String> menciones = eliminado
                ? Collections.emptyList()
                : mentionsForText(c.getTexto(), validMentions);

        return new CommentResponse(
                c.getId(),
                c.getComentarioPadre() != null ? c.getComentarioPadre().getId() : null,
                texto,
                c.getContadorLikes(),
                !eliminado && likedCommentIds.contains(c.getId()),
                eliminado,
                c.getFechaCreacion(),
                usuarioDto,
                menciones,
                replies.stream()
                        .map(reply -> toResponse(reply, Collections.emptyList(), validMentions, likedCommentIds))
                        .toList()
        );
    }

    private Set<Integer> likedCommentIds(Integer usuarioId, List<ComentarioEntity> comments) {
        List<Integer> commentIds = comments.stream()
                .filter(ComentarioEntity::isEstaActivo)
                .map(ComentarioEntity::getId)
                .toList();
        if (commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(likeRepository.findLikedReferenceIds(usuarioId, TipoLike.comentario, commentIds));
    }

    private Set<String> collectAllMentions(List<ComentarioEntity> comments) {
        Set<String> mentions = new LinkedHashSet<>();
        for (ComentarioEntity comment : comments) {
            if (comment.isEstaActivo()) {
                mentions.addAll(extractMentionsLowercase(comment.getTexto()));
            }
        }
        return mentions;
    }

    private Set<String> extractMentionsLowercase(String texto) {
        if (texto == null || texto.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(texto);
        while (matcher.find()) {
            result.add(matcher.group(1).toLowerCase(Locale.ROOT));
        }
        return result;
    }

    private Map<String, String> resolveValidMentions(Set<String> usernamesLowercase) {
        if (usernamesLowercase.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String realUsername : usuarioRepository.findExistingUsernames(usernamesLowercase)) {
            result.put(realUsername.toLowerCase(Locale.ROOT), realUsername);
        }
        return result;
    }

    private List<String> mentionsForText(String texto, Map<String, String> validMentions) {
        if (validMentions.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> alreadyAdded = new HashSet<>();
        List<String> result = new ArrayList<>();
        for (String mentionLower : extractMentionsLowercase(texto)) {
            String realUsername = validMentions.get(mentionLower);
            if (realUsername != null && alreadyAdded.add(realUsername)) {
                result.add(realUsername);
            }
        }
        return result;
    }
}
