package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.CommentCreateRequest;
import com.agg.dumbellcheck.dto.CommentResponse;
import com.agg.dumbellcheck.entities.ComentarioEntity;
import com.agg.dumbellcheck.entities.PublicacionEntity;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.repositories.ComentarioRepository;
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

    public CommentService(
            ComentarioRepository comentarioRepository,
            PublicacionRepository publicacionRepository,
            UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPublicId(String publicId) {
        PublicacionEntity publicacion = publicacionRepository.findByPublicIdAndEstaActivaTrue(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        List<ComentarioEntity> comments = comentarioRepository.findActiveByPublicacionId(publicacion.getId());
        Map<Integer, ComentarioEntity> rootEntities = new LinkedHashMap<>();
        Map<Integer, List<ComentarioEntity>> repliesByRootId = new LinkedHashMap<>();

        for (ComentarioEntity comment : comments) {
            ComentarioEntity parent = comment.getComentarioPadre();
            if (parent == null) {
                rootEntities.put(comment.getId(), comment);
                repliesByRootId.putIfAbsent(comment.getId(), new ArrayList<>());
            } else {
                Integer rootId = parent.getComentarioPadre() == null
                        ? parent.getId()
                        : parent.getComentarioPadre().getId();
                repliesByRootId.computeIfAbsent(rootId, ignored -> new ArrayList<>()).add(comment);
            }
        }

        Map<String, String> validMentions = resolveValidMentions(collectAllMentions(comments));

        return rootEntities.values().stream()
                .map(root -> toResponse(
                        root,
                        repliesByRootId.getOrDefault(root.getId(), Collections.emptyList()),
                        validMentions))
                .toList();
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
        return toResponse(comentario, Collections.emptyList(), validMentions);
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
            Map<String, String> validMentions) {
        UsuarioEntity u = c.getUsuario();
        return new CommentResponse(
                c.getId(),
                c.getComentarioPadre() != null ? c.getComentarioPadre().getId() : null,
                c.getTexto(),
                c.getContadorLikes(),
                c.getFechaCreacion(),
                new CommentResponse.UsuarioResumen(
                        u.getId(),
                        u.getUsername(),
                        u.getFotoPerfilUrl()
                ),
                mentionsForText(c.getTexto(), validMentions),
                replies.stream()
                        .map(reply -> toResponse(reply, Collections.emptyList(), validMentions))
                        .toList()
        );
    }

    private Set<String> collectAllMentions(List<ComentarioEntity> comments) {
        Set<String> mentions = new LinkedHashSet<>();
        for (ComentarioEntity comment : comments) {
            mentions.addAll(extractMentionsLowercase(comment.getTexto()));
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
