package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.PostCreateRequest;
import com.agg.dumbellcheck.dto.PostFeedItemResponse;
import com.agg.dumbellcheck.entities.DetalleSerieEntity;
import com.agg.dumbellcheck.entities.EjercicioEntity;
import com.agg.dumbellcheck.entities.EjercicioPublicacionEntity;
import com.agg.dumbellcheck.entities.PublicacionEntity;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.repositories.DetalleSerieRepository;
import com.agg.dumbellcheck.repositories.EjercicioPublicacionRepository;
import com.agg.dumbellcheck.repositories.EjercicioRepository;
import com.agg.dumbellcheck.repositories.PublicacionRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PostService {

    private final PublicacionRepository publicacionRepository;
    private final EjercicioRepository ejercicioRepository;
    private final EjercicioPublicacionRepository ejercicioPublicacionRepository;
    private final DetalleSerieRepository detalleSerieRepository;
    private final UsuarioRepository usuarioRepository;

    public PostService(
            PublicacionRepository publicacionRepository,
            EjercicioRepository ejercicioRepository,
            EjercicioPublicacionRepository ejercicioPublicacionRepository,
            DetalleSerieRepository detalleSerieRepository,
            UsuarioRepository usuarioRepository) {
        this.publicacionRepository = publicacionRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.ejercicioPublicacionRepository = ejercicioPublicacionRepository;
        this.detalleSerieRepository = detalleSerieRepository;
        this.usuarioRepository = usuarioRepository;
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

        // rutinaOrigenId reserved for future "post from template" flow

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

        return buildFeedItem(publicacion, ejerciciosResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostFeedItemResponse> getFeedPublico(Pageable pageable) {
        return publicacionRepository.findByEstaActivaTrueOrderByFechaCreacionDesc(pageable)
                .map(p -> {
                    return buildFeedItem(p, mapEjercicios(p));
                });
    }

    @Transactional(readOnly = true)
    public PostFeedItemResponse getPostById(Integer postId) {
        PublicacionEntity publicacion = publicacionRepository.findByIdAndEstaActivaTrue(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        return buildFeedItem(publicacion, mapEjercicios(publicacion));
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

    private PostFeedItemResponse buildFeedItem(PublicacionEntity p, List<PostFeedItemResponse.EjercicioEnPost> ejercicios) {
        return new PostFeedItemResponse(
                p.getId(),
                new PostFeedItemResponse.UsuarioResumen(
                        p.getUsuario().getId(),
                        p.getUsuario().getUsername(),
                        p.getUsuario().getFotoPerfilUrl()
                ),
                p.getTitulo(),
                p.getDescripcion(),
                p.getMultimediaUrl(),
                p.getContadorLikes(),
                p.getContadorComentarios(),
                p.getFechaCreacion(),
                ejercicios
        );
    }
}
