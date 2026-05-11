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
import java.util.List;

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

        return comentarioRepository.findActiveByPublicacionId(publicacion.getId()).stream()
                .map(this::toResponse)
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
        comentario.setContadorLikes(0);
        comentario.setEstaActivo(true);
        comentario.setFechaCreacion(Instant.now());

        comentarioRepository.save(comentario);
        return toResponse(comentario);
    }

    private CommentResponse toResponse(ComentarioEntity c) {
        UsuarioEntity u = c.getUsuario();
        return new CommentResponse(
                c.getId(),
                c.getTexto(),
                c.getContadorLikes(),
                c.getFechaCreacion(),
                new CommentResponse.UsuarioResumen(
                        u.getId(),
                        u.getUsername(),
                        u.getFotoPerfilUrl()
                )
        );
    }
}
