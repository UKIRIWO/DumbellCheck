package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.LikeToggleResponse;
import com.agg.dumbellcheck.entities.ComentarioEntity;
import com.agg.dumbellcheck.entities.LikeEntity;
import com.agg.dumbellcheck.entities.PublicacionEntity;
import com.agg.dumbellcheck.entities.TipoLike;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.repositories.ComentarioRepository;
import com.agg.dumbellcheck.repositories.LikeRepository;
import com.agg.dumbellcheck.repositories.PublicacionRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;

    public LikeService(
            LikeRepository likeRepository,
            PublicacionRepository publicacionRepository,
            ComentarioRepository comentarioRepository,
            UsuarioRepository usuarioRepository) {
        this.likeRepository = likeRepository;
        this.publicacionRepository = publicacionRepository;
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public LikeToggleResponse togglePublicationLike(String publicId, String username) {
        PublicacionEntity publicacion = publicacionRepository.findByPublicIdAndEstaActivaTrue(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        UsuarioEntity usuario = findUser(username);

        boolean liked = toggleLike(usuario, TipoLike.publicacion, publicacion.getId());
        publicacion.setContadorLikes(nextCounter(publicacion.getContadorLikes(), liked));
        publicacionRepository.save(publicacion);

        return new LikeToggleResponse(liked, publicacion.getContadorLikes());
    }

    @Transactional
    public LikeToggleResponse toggleCommentLike(String publicId, Integer commentId, String username) {
        PublicacionEntity publicacion = publicacionRepository.findByPublicIdAndEstaActivaTrue(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        ComentarioEntity comentario = comentarioRepository
                .findByIdAndPublicacionIdAndEstaActivoTrue(commentId, publicacion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));
        UsuarioEntity usuario = findUser(username);

        boolean liked = toggleLike(usuario, TipoLike.comentario, comentario.getId());
        comentario.setContadorLikes(nextCounter(comentario.getContadorLikes(), liked));
        comentarioRepository.save(comentario);

        return new LikeToggleResponse(liked, comentario.getContadorLikes());
    }

    private UsuarioEntity findUser(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private boolean toggleLike(UsuarioEntity usuario, TipoLike tipo, Integer referenciaId) {
        return likeRepository
                .findByUsuarioIdAndTipoAndReferenciaId(usuario.getId(), tipo, referenciaId)
                .map(existing -> {
                    likeRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    LikeEntity like = new LikeEntity();
                    like.setUsuario(usuario);
                    like.setTipo(tipo);
                    like.setReferenciaId(referenciaId);
                    like.setFechaCreacion(Instant.now());
                    likeRepository.save(like);
                    return true;
                });
    }

    private Integer nextCounter(Integer current, boolean increment) {
        int value = current == null ? 0 : current;
        return increment ? value + 1 : Math.max(0, value - 1);
    }
}
