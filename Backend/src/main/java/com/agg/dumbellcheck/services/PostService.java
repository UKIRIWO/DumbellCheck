package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.CursorPageResponse;
import com.agg.dumbellcheck.dto.PostCreateRequest;
import com.agg.dumbellcheck.dto.PostFeedItemResponse;
import com.agg.dumbellcheck.entities.DetalleSerieEntity;
import com.agg.dumbellcheck.entities.EjercicioEntity;
import com.agg.dumbellcheck.entities.EjercicioPublicacionEntity;
import com.agg.dumbellcheck.entities.PublicacionEntity;
import com.agg.dumbellcheck.entities.TipoLike;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.repositories.DetalleSerieRepository;
import com.agg.dumbellcheck.repositories.EjercicioPublicacionRepository;
import com.agg.dumbellcheck.repositories.EjercicioRepository;
import com.agg.dumbellcheck.repositories.LikeRepository;
import com.agg.dumbellcheck.repositories.PublicacionRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PostService {

    private final PublicacionRepository publicacionRepository;
    private final EjercicioRepository ejercicioRepository;
    private final EjercicioPublicacionRepository ejercicioPublicacionRepository;
    private final DetalleSerieRepository detalleSerieRepository;
    private final UsuarioRepository usuarioRepository;
    private final LikeRepository likeRepository;

    public PostService(
            PublicacionRepository publicacionRepository,
            EjercicioRepository ejercicioRepository,
            EjercicioPublicacionRepository ejercicioPublicacionRepository,
            DetalleSerieRepository detalleSerieRepository,
            UsuarioRepository usuarioRepository,
            LikeRepository likeRepository) {
        this.publicacionRepository = publicacionRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.ejercicioPublicacionRepository = ejercicioPublicacionRepository;
        this.detalleSerieRepository = detalleSerieRepository;
        this.usuarioRepository = usuarioRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public PostFeedItemResponse createPost(String username, PostCreateRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        PublicacionEntity publicacion = new PublicacionEntity();
        publicacion.setUsuario(usuario);
        publicacion.setTitulo(request.titulo().trim());
        publicacion.setDescripcion(request.descripcion());
        publicacion.setMultimediaUrl(request.multimediaUrl());
        publicacion.setContadorLikes(0);
        publicacion.setContadorComentarios(0);
        publicacion.setEstaActiva(true);
        publicacion.setFechaCreacion(Instant.now());

        publicacionRepository.save(publicacion);

        List<PostFeedItemResponse.EjercicioEnPost> ejerciciosResponse = new ArrayList<>();

        for (int i = 0; i < request.ejercicios().size(); i++) {
            PostCreateRequest.EjercicioRequest ejReq = request.ejercicios().get(i);
            final int ejercicioId = ejReq.ejercicioId();

            EjercicioEntity ejercicio = ejercicioRepository.findById(ejercicioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado: " + ejercicioId));

            EjercicioPublicacionEntity ejPub = new EjercicioPublicacionEntity();
            ejPub.setPublicacion(publicacion);
            ejPub.setEjercicio(ejercicio);
            ejPub.setOrden(ejReq.orden() != null ? ejReq.orden() : i);
            ejPub.setNotas(ejReq.notas());
            ejPub.setFechaCreacion(Instant.now());

            ejercicioPublicacionRepository.save(ejPub);

            List<PostFeedItemResponse.SerieEnPost> seriesResponse = new ArrayList<>();
            for (PostCreateRequest.SerieRequest serieReq : ejReq.series()) {
                DetalleSerieEntity detalle = new DetalleSerieEntity();
                detalle.setEjercicioPublicacion(ejPub);
                detalle.setNumeroSerie(serieReq.numeroSerie());
                detalle.setRepeticiones(serieReq.repeticiones());
                detalle.setPeso(serieReq.peso());
                detalle.setDescansoSegundos(serieReq.descansoSegundos());
                detalle.setRpe(serieReq.rpe());
                detalle.setNotas(serieReq.notas());
                detalle.setFechaCreacion(Instant.now());

                detalleSerieRepository.save(detalle);

                seriesResponse.add(new PostFeedItemResponse.SerieEnPost(
                        serieReq.numeroSerie(),
                        serieReq.repeticiones(),
                        serieReq.peso(),
                        serieReq.descansoSegundos()
                ));
            }

            ejerciciosResponse.add(new PostFeedItemResponse.EjercicioEnPost(
                    ejPub.getId(),
                    ejercicio.getId(),
                    ejercicio.getNombre(),
                    ejercicio.getImagenUrl(),
                    ejPub.getOrden(),
                    ejPub.getNotas(),
                    seriesResponse
            ));
        }

        return buildFeedItem(publicacion, ejerciciosResponse, false);
    }

    @Transactional(readOnly = true)
    public Page<PostFeedItemResponse> getFeedPublico(String currentUsername, Pageable pageable) {
        UsuarioEntity currentUser = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Page<PublicacionEntity> page = publicacionRepository.findByEstaActivaTrueOrderByFechaCreacionDesc(pageable);
        Set<Integer> likedPostIds = likedPostIds(currentUser.getId(), page.getContent());
        return page.map(p -> buildFeedItem(p, mapEjercicios(p), likedPostIds.contains(p.getId())));
    }

    @Transactional(readOnly = true)
    public Page<PostFeedItemResponse> getFeedAmigos(String username, Pageable pageable) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Page<PublicacionEntity> page = publicacionRepository.findFeedAmigos(usuario.getId(), pageable);
        Set<Integer> likedPostIds = likedPostIds(usuario.getId(), page.getContent());
        return page.map(p -> buildFeedItem(p, mapEjercicios(p), likedPostIds.contains(p.getId())));
    }

    @Transactional(readOnly = true)
    public Page<PostFeedItemResponse> getFeedDescubrir(String username, Pageable pageable) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Page<PublicacionEntity> page = publicacionRepository.findFeedDescubrir(usuario.getId(), pageable);
        Set<Integer> likedPostIds = likedPostIds(usuario.getId(), page.getContent());
        return page.map(p -> buildFeedItem(p, mapEjercicios(p), likedPostIds.contains(p.getId())));
    }

    @Transactional(readOnly = true)
    public PostFeedItemResponse getPostByPublicId(String publicId, String currentUsername) {
        PublicacionEntity publicacion = publicacionRepository.findByPublicIdAndEstaActivaTrue(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        UsuarioEntity currentUser = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        boolean liked = likeRepository.existsByUsuarioIdAndTipoAndReferenciaId(
                currentUser.getId(),
                TipoLike.publicacion,
                publicacion.getId());
        return buildFeedItem(publicacion, mapEjercicios(publicacion), liked);
    }

    private List<PostFeedItemResponse.EjercicioEnPost> mapEjercicios(PublicacionEntity publicacion) {
        return publicacion.getEjercicios().stream()
                .sorted(Comparator.comparingInt(EjercicioPublicacionEntity::getOrden))
                .map(ep -> {
                    List<PostFeedItemResponse.SerieEnPost> series = ep.getDetallesSeries().stream()
                            .sorted(Comparator.comparingInt(DetalleSerieEntity::getNumeroSerie))
                            .map(ds -> new PostFeedItemResponse.SerieEnPost(
                                    ds.getNumeroSerie(),
                                    ds.getRepeticiones(),
                                    ds.getPeso(),
                                    ds.getDescansoSegundos()
                            ))
                            .toList();
                    return new PostFeedItemResponse.EjercicioEnPost(
                            ep.getId(),
                            ep.getEjercicio().getId(),
                            ep.getEjercicio().getNombre(),
                            ep.getEjercicio().getImagenUrl(),
                            ep.getOrden(),
                            ep.getNotas(),
                            series
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostFeedItemResponse> getPostsByUser(
            String username,
            String currentUsername,
            Integer cursor,
            int size) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        UsuarioEntity currentUser = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        int pageSize = Math.min(size, 30);
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        List<PublicacionEntity> rows = (cursor == null)
                ? publicacionRepository.findByUsuarioIdAndEstaActivaTrueOrderByIdDesc(usuario.getId(), pageable)
                : publicacionRepository.findByUsuarioIdAndEstaActivaTrueAndIdLessThanOrderByIdDesc(usuario.getId(), cursor, pageable);

        boolean hasMore = rows.size() > pageSize;
        List<PublicacionEntity> page = hasMore ? rows.subList(0, pageSize) : rows;
        Integer nextCursor = hasMore ? page.get(page.size() - 1).getId() : null;

        Set<Integer> likedPostIds = likedPostIds(currentUser.getId(), page);
        List<PostFeedItemResponse> content = page.stream()
                .map(p -> buildFeedItem(p, mapEjercicios(p), likedPostIds.contains(p.getId())))
                .toList();

        return new CursorPageResponse<>(content, nextCursor, hasMore);
    }

    private PostFeedItemResponse buildFeedItem(
            PublicacionEntity p,
            List<PostFeedItemResponse.EjercicioEnPost> ejercicios,
            boolean liked) {
        return new PostFeedItemResponse(
                p.getId(),
                p.getPublicId(),
                new PostFeedItemResponse.UsuarioResumen(
                        p.getUsuario().getId(),
                        p.getUsuario().getUsername(),
                        p.getUsuario().getFotoPerfilUrl()
                ),
                p.getTitulo(),
                p.getDescripcion(),
                p.getMultimediaUrl(),
                p.getContadorLikes(),
                liked,
                p.getContadorComentarios(),
                p.getFechaCreacion(),
                ejercicios
        );
    }

    private Set<Integer> likedPostIds(Integer usuarioId, List<PublicacionEntity> publicaciones) {
        if (publicaciones.isEmpty()) {
            return Collections.emptySet();
        }
        List<Integer> postIds = publicaciones.stream()
                .map(PublicacionEntity::getId)
                .toList();
        return new HashSet<>(likeRepository.findLikedReferenceIds(usuarioId, TipoLike.publicacion, postIds));
    }
}
