package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.UserInfoDTO.*;
import com.agg.dumbellcheck.entities.SeguidorEntity;
import com.agg.dumbellcheck.entities.UsuarioEnlaceEntity;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceConflictException;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.exceptions.UnauthorizedActionException;
import com.agg.dumbellcheck.mapper.UserInfoMapper;
import com.agg.dumbellcheck.repositories.SeguidorRepository;
import com.agg.dumbellcheck.repositories.UsuarioEnlaceRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Comparator;
import java.util.Set;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class PerfilService {

    private static final int MAX_ENLACES = 10;

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEnlaceRepository enlaceRepository;
    private final SeguidorRepository seguidorRepository;
    private final UserInfoMapper mapper;
    private final MediaStorageService mediaStorageService;
    private final EntityManager entityManager;

    public PerfilService(
            UsuarioRepository usuarioRepository,
            UsuarioEnlaceRepository enlaceRepository,
            SeguidorRepository seguidorRepository,
            UserInfoMapper mapper,
            MediaStorageService mediaStorageService,
            EntityManager entityManager) {
        this.usuarioRepository = usuarioRepository;
        this.enlaceRepository = enlaceRepository;
        this.seguidorRepository = seguidorRepository;
        this.mapper = mapper;
        this.mediaStorageService = mediaStorageService;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public PerfilDto getPerfil(String viewerUsername, String targetUsername) {
        UsuarioEntity viewer = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        UsuarioEntity target = usuarioRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + targetUsername));

        boolean esPropio = viewer.getId().equals(target.getId());
        boolean sigueAEsteUsuario = !esPropio &&
                seguidorRepository.existsByUsuarioIdAndSeguidoId(viewer.getId(), target.getId());

        List<UsuarioEnlaceDto> enlaces = enlaceRepository
                .findByUsuarioIdOrderByOrdenAsc(target.getId())
                .stream()
                .map(mapper::toUsuarioEnlaceDto)
                .toList();

        return new PerfilDto(
                target.getId(),
                target.getUsername(),
                target.getNombre(),
                target.getApellido1(),
                target.getApellido2(),
                target.getFotoPerfilUrl(),
                target.getBannerUrl(),
                target.getBiografia(),
                target.getContadorSeguidores(),
                target.getContadorSeguidos(),
                target.getContadorPublicaciones(),
                target.getFechaCreacion(),
                enlaces,
                esPropio,
                sigueAEsteUsuario
        );
    }

    @Transactional
    public PerfilDto updateMyProfile(String viewerUsername, PerfilUpdateRequest request) {
        UsuarioEntity user = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setNombre(request.nombre().trim());
        user.setApellido1(request.apellido1().trim());
        user.setApellido2(request.apellido2() != null ? request.apellido2().trim() : null);
        user.setBiografia(request.biografia() != null ? request.biografia().trim() : null);

        usuarioRepository.save(user);
        return getPerfil(viewerUsername, viewerUsername);
    }

    @Transactional
    public String uploadFotoPerfil(String viewerUsername, MultipartFile file) {
        UsuarioEntity user = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String url = mediaStorageService.storeProfilePicture(file);
        user.setFotoPerfilUrl(url);
        usuarioRepository.save(user);
        return url;
    }

    @Transactional
    public String uploadBanner(String viewerUsername, MultipartFile file) {
        UsuarioEntity user = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String url = mediaStorageService.storeBanner(file);
        user.setBannerUrl(url);
        usuarioRepository.save(user);
        return url;
    }

    @Transactional
    public UsuarioEnlaceDto addEnlace(String viewerUsername, UsuarioEnlaceRequest request) {
        UsuarioEntity user = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        long currentCount = enlaceRepository.countByUsuarioId(user.getId());
        if (currentCount >= MAX_ENLACES) {
            throw new ResourceConflictException("No puedes tener más de " + MAX_ENLACES + " enlaces");
        }

        if (enlaceRepository.existsByUsuarioIdAndUrl(user.getId(), request.url())) {
            throw new ResourceConflictException("Ya existe un enlace con esa URL");
        }

        UsuarioEnlaceEntity enlace = new UsuarioEnlaceEntity();
        enlace.setUsuario(user);
        enlace.setPlataforma(request.plataforma());
        enlace.setUrl(request.url());
        enlace.setOrden((byte) currentCount);

        return mapper.toUsuarioEnlaceDto(enlaceRepository.save(enlace));
    }

    @Transactional
    public void deleteEnlace(String viewerUsername, Integer enlaceId) {
        UsuarioEntity user = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UsuarioEnlaceEntity enlace = enlaceRepository.findById(enlaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Enlace no encontrado"));

        if (!enlace.getUsuario().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("No puedes eliminar un enlace que no es tuyo");
        }

        enlaceRepository.delete(enlace);
    }

    @Transactional
    public PerfilDto seguirUsuario(String viewerUsername, String targetUsername) {
        UsuarioEntity viewer = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        UsuarioEntity target = usuarioRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + targetUsername));

        if (viewer.getId().equals(target.getId())) {
            throw new ResourceConflictException("No puedes seguirte a ti mismo");
        }

        if (!seguidorRepository.existsByUsuarioIdAndSeguidoId(viewer.getId(), target.getId())) {
            SeguidorEntity seguidor = new SeguidorEntity();
            seguidor.setUsuario(viewer);
            seguidor.setSeguido(target);
            seguidor.setFechaSeguimiento(Instant.now());
            seguidorRepository.save(seguidor);
        }


        entityManager.flush();
        entityManager.clear();
        return getPerfil(viewerUsername, targetUsername);
    }

    @Transactional
    public PerfilDto dejarDeSeguirUsuario(String viewerUsername, String targetUsername) {
        UsuarioEntity viewer = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        UsuarioEntity target = usuarioRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + targetUsername));

        if (viewer.getId().equals(target.getId())) {
            throw new ResourceConflictException("No puedes dejar de seguirte a ti mismo");
        }

        seguidorRepository.findByUsuarioIdAndSeguidoId(viewer.getId(), target.getId())
                .ifPresent(seguidorRepository::delete);

        entityManager.flush();
        entityManager.clear();
        return getPerfil(viewerUsername, targetUsername);
    }

    @Transactional(readOnly = true)
    public List<PerfilConnectionDto> getSeguidores(String viewerUsername, String targetUsername) {
        UsuarioEntity viewer = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        UsuarioEntity target = usuarioRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + targetUsername));

        List<UsuarioEntity> usuarios = seguidorRepository.findSeguidoresUsuariosByUsuarioId(target.getId());
        return mapConnections(viewer.getId(), usuarios);
    }

    @Transactional(readOnly = true)
    public List<PerfilConnectionDto> getSeguidos(String viewerUsername, String targetUsername) {
        UsuarioEntity viewer = usuarioRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        UsuarioEntity target = usuarioRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + targetUsername));

        List<UsuarioEntity> usuarios = seguidorRepository.findSeguidosUsuariosByUsuarioId(target.getId());
        return mapConnections(viewer.getId(), usuarios);
    }

    private List<PerfilConnectionDto> mapConnections(Integer viewerId, List<UsuarioEntity> usuarios) {
        Set<Integer> seguidosPorMi = seguidorRepository.findSeguidoIdsByUsuarioId(viewerId).stream()
                .collect(Collectors.toSet());

        return usuarios.stream()
                .map(u -> new PerfilConnectionDto(
                        u.getId(),
                        u.getUsername(),
                        u.getNombre(),
                        u.getFotoPerfilUrl(),
                        seguidosPorMi.contains(u.getId()),
                        viewerId.equals(u.getId())))
                .sorted(Comparator
                        .comparing(PerfilConnectionDto::esPropio).reversed()
                        .thenComparing(Comparator.comparing(PerfilConnectionDto::seguidoPorMi).reversed())
                        .thenComparing(PerfilConnectionDto::username, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
