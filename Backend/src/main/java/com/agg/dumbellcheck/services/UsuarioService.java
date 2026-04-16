package com.agg.dumbellcheck.services;

import org.springframework.stereotype.Service;

import com.agg.dumbellcheck.dto.UserInfoDTO.UsuarioDto;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.mapper.UserInfoMapper;
import com.agg.dumbellcheck.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UserInfoMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository, UserInfoMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    public UsuarioDto getUsuarioById(Integer id) {
        return usuarioRepository.findByIdWithBaneos(id)
                .map(usuarioMapper::toUsuarioDto)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
