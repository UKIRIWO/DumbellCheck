package com.agg.dumbellcheck.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agg.dumbellcheck.dto.UserInfoDTO.SidebarDataDto;
import com.agg.dumbellcheck.dto.UserInfoDTO.SidebarProfileDto;
import com.agg.dumbellcheck.dto.UserInfoDTO.SidebarSuggestionDto;
import com.agg.dumbellcheck.dto.UserInfoDTO.UsuarioDto;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.mapper.UserInfoMapper;
import com.agg.dumbellcheck.repositories.SeguidorRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;

import java.util.Collections;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SeguidorRepository seguidorRepository;
    private final UserInfoMapper usuarioMapper;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            SeguidorRepository seguidorRepository,
            UserInfoMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.seguidorRepository = seguidorRepository;
        this.usuarioMapper = usuarioMapper;
    }

    public UsuarioDto getUsuarioById(Integer id) {
        return usuarioRepository.findByIdWithBaneos(id)
                .map(usuarioMapper::toUsuarioDto)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public SidebarDataDto getSidebarData(String username, int limit) {
        UsuarioEntity currentUser = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<SidebarSuggestionDto> sugerencias = usuarioRepository.findSuggestedUsers(currentUser.getId(), limit).stream()
                .map(user -> new SidebarSuggestionDto(
                        user.getId(),
                        user.getUsername(),
                        user.getNombre(),
                        user.getFotoPerfilUrl()))
                .toList();

        SidebarProfileDto perfil = new SidebarProfileDto(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getNombre(),
                currentUser.getFotoPerfilUrl(),
                (int) seguidorRepository.countBySeguidoId(currentUser.getId()),
                (int) seguidorRepository.countByUsuarioId(currentUser.getId()),
                currentUser.getContadorPublicaciones());

        return new SidebarDataDto(perfil, sugerencias);
    }

    @Transactional(readOnly = true)
    public List<SidebarSuggestionDto> searchUsuarios(String query, int limit) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        int safeLimit = Math.max(1, Math.min(limit, 30));
        return usuarioRepository.searchUsuarios(trimmed, PageRequest.of(0, safeLimit)).stream()
                .map(user -> new SidebarSuggestionDto(
                        user.getId(),
                        user.getUsername(),
                        user.getNombre(),
                        user.getFotoPerfilUrl()))
                .toList();
    }
}
