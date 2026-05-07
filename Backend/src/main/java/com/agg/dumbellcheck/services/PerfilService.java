package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.UserInfoDTO.*;
import com.agg.dumbellcheck.entities.UsuarioEnlaceEntity;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceConflictException;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.exceptions.UnauthorizedActionException;
import com.agg.dumbellcheck.mapper.UserInfoMapper;
import com.agg.dumbellcheck.repositories.SeguidorRepository;
import com.agg.dumbellcheck.repositories.UsuarioEnlaceRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PerfilService {

    private static final int MAX_ENLACES = 10;

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEnlaceRepository enlaceRepository;
    private final SeguidorRepository seguidorRepository;
    private final UserInfoMapper mapper;
    private final MediaStorageService mediaStorageService;

    public PerfilService(
            UsuarioRepository usuarioRepository,
            UsuarioEnlaceRepository enlaceRepository,
            SeguidorRepository seguidorRepository,
            UserInfoMapper mapper,
            MediaStorageService mediaStorageService) {
        this.usuarioRepository = usuarioRepository;
        this.enlaceRepository = enlaceRepository;
        this.seguidorRepository = seguidorRepository;
        this.mapper = mapper;
        this.mediaStorageService = mediaStorageService;
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
}
