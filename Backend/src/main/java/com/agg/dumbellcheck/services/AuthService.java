package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.AuthLoginRequest;
import com.agg.dumbellcheck.dto.AuthLoginResponse;
import com.agg.dumbellcheck.dto.AuthRegisterRequest;
import com.agg.dumbellcheck.dto.UserInfoDTO.UsuarioDto;
import com.agg.dumbellcheck.entities.RolUsuario;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceConflictException;
import com.agg.dumbellcheck.exceptions.UnauthorizedActionException;
import com.agg.dumbellcheck.mapper.UserInfoMapper;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import com.agg.dumbellcheck.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UserInfoMapper userInfoMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            UserInfoMapper userInfoMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.userInfoMapper = userInfoMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioDto register(AuthRegisterRequest request) {
        String normalizedUsername = request.username().trim();
        String normalizedEmail = request.email().trim().toLowerCase();

        if (usuarioRepository.existsByUsername(normalizedUsername)) {
            throw new ResourceConflictException("El nombre de usuario ya existe");
        }
        if (usuarioRepository.existsByEmail(normalizedEmail)) {
            throw new ResourceConflictException("El email ya existe");
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setUsername(normalizedUsername);
        usuario.setEmail(normalizedEmail);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido1(request.apellido1().trim());
        usuario.setApellido2(request.apellido2() == null ? null : request.apellido2().trim());
        usuario.setRol(RolUsuario.MEMBER);
        usuario.setContadorSeguidores(0);
        usuario.setContadorSeguidos(0);
        usuario.setEstaActivo(true);
        usuario.setFechaCreacion(Instant.now());
        usuario.setPassword(passwordEncoder.encode(request.password()));

        UsuarioEntity saved = usuarioRepository.save(usuario);
        return userInfoMapper.toUsuarioDto(saved);
    }

    @Transactional(readOnly = true)
    public AuthLoginResponse login(AuthLoginRequest request) {
        String principal = request.principal().trim();
        UsuarioEntity usuario = usuarioRepository.findByUsername(principal)
                .or(() -> usuarioRepository.findByEmailIgnoreCase(principal))
                .orElseThrow(() -> new UnauthorizedActionException("Credenciales incorrectas"));

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new UnauthorizedActionException("Credenciales incorrectas");
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new UnauthorizedActionException("Credenciales incorrectas");
        }

        String accessToken = jwtService.generateToken(
                usuario.getUsername(),
                Map.of(
                        "userId", usuario.getId(),
                        "email", usuario.getEmail(),
                        "role", usuario.getRol().name()
                )
        );

        usuario.setUltimaConexion(Instant.now());

        return new AuthLoginResponse(
                accessToken,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol()
        );
    }
}
