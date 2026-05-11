package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.RutinaCreateRequest;
import com.agg.dumbellcheck.dto.RutinaListItemResponse;
import com.agg.dumbellcheck.dto.RutinaResponse;
import com.agg.dumbellcheck.entities.DetalleSerieRutinaEntity;
import com.agg.dumbellcheck.entities.EjercicioEntity;
import com.agg.dumbellcheck.entities.EjercicioRutinaEntity;
import com.agg.dumbellcheck.entities.RutinaEntity;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.exceptions.UnauthorizedActionException;
import com.agg.dumbellcheck.repositories.DetalleSerieRutinaRepository;
import com.agg.dumbellcheck.repositories.EjercicioRepository;
import com.agg.dumbellcheck.repositories.EjercicioRutinaRepository;
import com.agg.dumbellcheck.repositories.RutinaRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class RutinaService {

    @PersistenceContext
    private EntityManager entityManager;

    private final RutinaRepository rutinaRepository;
    private final EjercicioRutinaRepository ejercicioRutinaRepository;
    private final DetalleSerieRutinaRepository detalleSerieRutinaRepository;
    private final EjercicioRepository ejercicioRepository;
    private final UsuarioRepository usuarioRepository;

    public RutinaService(
            RutinaRepository rutinaRepository,
            EjercicioRutinaRepository ejercicioRutinaRepository,
            DetalleSerieRutinaRepository detalleSerieRutinaRepository,
            EjercicioRepository ejercicioRepository,
            UsuarioRepository usuarioRepository) {
        this.rutinaRepository = rutinaRepository;
        this.ejercicioRutinaRepository = ejercicioRutinaRepository;
        this.detalleSerieRutinaRepository = detalleSerieRutinaRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public RutinaResponse createRutina(String username, RutinaCreateRequest request) {
        UsuarioEntity usuario = findUser(username);

        RutinaEntity rutina = new RutinaEntity();
        rutina.setUsuario(usuario);
        rutina.setNombre(request.nombre().trim());
        rutina.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : null);
        rutina.setEsPublica(true);
        rutina.setFechaCreacion(Instant.now());
        rutinaRepository.save(rutina);

        List<EjercicioRutinaEntity> ejerciciosSaved = saveEjercicios(rutina, request.ejercicios());
        return toDetailResponse(rutina, ejerciciosSaved);
    }

    @Transactional(readOnly = true)
    public List<RutinaListItemResponse> getMyRutinas(String username) {
        UsuarioEntity usuario = findUser(username);
        return rutinaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .map(r -> toListItemResponse(r, ejercicioRutinaRepository.findByRutinaIdOrderByOrden(r.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RutinaListItemResponse> getPublicRutinas() {
        return rutinaRepository.findAllPublicasOrderByFechaCreacionDesc()
                .stream()
                .map(r -> toListItemResponse(r, ejercicioRutinaRepository.findByRutinaIdOrderByOrden(r.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RutinaResponse getRutinaByPublicId(String publicId) {
        RutinaEntity rutina = findByPublicId(publicId);
        List<EjercicioRutinaEntity> ejercicios = ejercicioRutinaRepository.findByRutinaIdOrderByOrden(rutina.getId());
        return toDetailResponse(rutina, ejercicios);
    }

    @Transactional
    public RutinaResponse updateRutina(String publicId, String username, RutinaCreateRequest request) {
        RutinaEntity rutina = findByPublicId(publicId);
        requireOwner(rutina, username);

        rutina.setNombre(request.nombre().trim());
        rutina.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : null);
        rutinaRepository.save(rutina);

        // Delete all series and exercises in bulk, then flush so the DB reflects the
        // deletes before we insert new rows (avoids unique-key violations).
        detalleSerieRutinaRepository.deleteSeriesByRutinaId(rutina.getId());
        ejercicioRutinaRepository.deleteByRutinaId(rutina.getId());
        entityManager.flush();
        entityManager.clear();

        List<EjercicioRutinaEntity> ejerciciosSaved = saveEjercicios(rutina, request.ejercicios());
        return toDetailResponse(rutina, ejerciciosSaved);
    }

    @Transactional
    public void deleteRutina(String publicId, String username) {
        RutinaEntity rutina = findByPublicId(publicId);
        requireOwner(rutina, username);

        detalleSerieRutinaRepository.deleteSeriesByRutinaId(rutina.getId());
        ejercicioRutinaRepository.deleteByRutinaId(rutina.getId());
        rutinaRepository.delete(rutina);
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    public RutinaEntity findByPublicId(String publicId) {
        return rutinaRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));
    }

    private UsuarioEntity findUser(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private void requireOwner(RutinaEntity rutina, String username) {
        if (!rutina.getUsuario().getUsername().equals(username)) {
            throw new UnauthorizedActionException("No puedes modificar esta rutina");
        }
    }

    private List<EjercicioRutinaEntity> saveEjercicios(
            RutinaEntity rutina,
            List<RutinaCreateRequest.EjercicioRutinaRequest> ejercicioRequests) {

        List<EjercicioRutinaEntity> saved = new java.util.ArrayList<>();

        for (int i = 0; i < ejercicioRequests.size(); i++) {
            RutinaCreateRequest.EjercicioRutinaRequest ejReq = ejercicioRequests.get(i);
            final int ejercicioId = ejReq.ejercicioId();

            EjercicioEntity ejercicio = ejercicioRepository.findById(ejercicioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado: " + ejercicioId));

            EjercicioRutinaEntity ejRutina = new EjercicioRutinaEntity();
            ejRutina.setRutina(rutina);
            ejRutina.setEjercicio(ejercicio);
            ejRutina.setOrden(ejReq.orden() != null ? ejReq.orden() : i);
            ejRutina.setNotas(ejReq.notas());
            ejRutina.setFechaCreacion(Instant.now());
            ejercicioRutinaRepository.save(ejRutina);

            List<DetalleSerieRutinaEntity> series = new java.util.ArrayList<>();
            for (RutinaCreateRequest.SerieRutinaRequest serieReq : ejReq.series()) {
                DetalleSerieRutinaEntity detalle = new DetalleSerieRutinaEntity();
                detalle.setEjercicioRutina(ejRutina);
                detalle.setNumeroSerie(serieReq.numeroSerie());
                detalle.setRepeticiones(serieReq.repeticiones());
                detalle.setPeso(serieReq.peso());
                detalle.setDescansoSegundos(serieReq.descansoSegundos());
                detalle.setRpe(serieReq.rpe());
                detalle.setNotas(serieReq.notas());
                detalle.setFechaCreacion(Instant.now());
                detalleSerieRutinaRepository.save(detalle);
                series.add(detalle);
            }

            ejRutina.setDetallesSeries(series);
            saved.add(ejRutina);
        }

        return saved;
    }

    private RutinaResponse toDetailResponse(RutinaEntity rutina, List<EjercicioRutinaEntity> ejercicios) {
        UsuarioEntity u = rutina.getUsuario();
        List<RutinaResponse.EjercicioRutinaResponse> ejResponses = ejercicios.stream()
                .sorted(Comparator.comparingInt(EjercicioRutinaEntity::getOrden))
                .map(ej -> new RutinaResponse.EjercicioRutinaResponse(
                        ej.getId(),
                        ej.getEjercicio().getId(),
                        ej.getEjercicio().getNombre(),
                        ej.getEjercicio().getImagenUrl(),
                        ej.getOrden(),
                        ej.getNotas(),
                        ej.getDetallesSeries().stream()
                                .sorted(Comparator.comparingInt(DetalleSerieRutinaEntity::getNumeroSerie))
                                .map(s -> new RutinaResponse.SerieRutinaResponse(
                                        s.getNumeroSerie(),
                                        s.getRepeticiones(),
                                        s.getPeso(),
                                        s.getDescansoSegundos(),
                                        s.getRpe(),
                                        s.getNotas()))
                                .toList()
                ))
                .toList();

        return new RutinaResponse(
                rutina.getId(),
                rutina.getPublicId(),
                rutina.getNombre(),
                rutina.getDescripcion(),
                rutina.isEsPublica(),
                rutina.getFechaCreacion(),
                new RutinaResponse.UsuarioResumen(u.getId(), u.getUsername(), u.getFotoPerfilUrl()),
                ejResponses
        );
    }

    private RutinaListItemResponse toListItemResponse(RutinaEntity rutina, List<EjercicioRutinaEntity> ejercicios) {
        UsuarioEntity u = rutina.getUsuario();
        List<String> nombres = ejercicios.stream()
                .sorted(Comparator.comparingInt(EjercicioRutinaEntity::getOrden))
                .map(ej -> ej.getEjercicio().getNombre())
                .toList();
        return new RutinaListItemResponse(
                rutina.getId(),
                rutina.getPublicId(),
                rutina.getNombre(),
                rutina.getDescripcion(),
                rutina.isEsPublica(),
                rutina.getFechaCreacion(),
                new RutinaListItemResponse.UsuarioResumen(u.getId(), u.getUsername(), u.getFotoPerfilUrl()),
                nombres
        );
    }
}
