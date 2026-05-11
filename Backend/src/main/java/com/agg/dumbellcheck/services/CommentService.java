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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

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

        return rootEntities.values().stream()
                .map(root -> toResponse(root, repliesByRootId.getOrDefault(root.getId(), Collections.emptyList())))
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
        return toResponse(comentario, Collections.emptyList());
    }

    private ComentarioEntity resolveRootComment(ComentarioEntity comment) {
        ComentarioEntity root = comment;
        while (root.getComentarioPadre() != null) {
            root = root.getComentarioPadre();
        }
        return root;
    }

    private CommentResponse toResponse(ComentarioEntity c, List<ComentarioEntity> replies) {
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
                replies.stream()
                        .map(reply -> toResponse(reply, Collections.emptyList()))
                        .toList()
        );
    }
}
