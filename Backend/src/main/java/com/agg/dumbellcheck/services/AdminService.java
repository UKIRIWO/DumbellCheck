package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.AdminDTO.*;
import com.agg.dumbellcheck.dto.UserInfoDTO.BaneoDto;
import com.agg.dumbellcheck.dto.UserInfoDTO.UsuarioDto;
import com.agg.dumbellcheck.entities.*;
import com.agg.dumbellcheck.exceptions.ResourceConflictException;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.mapper.UserInfoMapper;
import com.agg.dumbellcheck.repositories.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private static final Set<String> USER_SORT    = Set.of("id", "username", "email", "rol", "estaActivo", "fechaCreacion");
    private static final Set<String> POST_SORT    = Set.of("id", "titulo", "contadorLikes", "contadorComentarios", "estaActiva", "fechaCreacion");
    private static final Set<String> COMMENT_SORT = Set.of("id", "texto", "estaActivo", "contadorLikes", "fechaCreacion");
    private static final Set<String> BAN_SORT     = Set.of("id", "baneadoPermanentemente", "fechaCreacion");
    private static final Set<String> EJ_SORT      = Set.of("id", "nombre", "fechaCreacion");

    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final BaneoRepository baneoRepository;
    private final EjercicioRepository ejercicioRepository;
    private final UserInfoMapper userInfoMapper;

    public AdminService(
            UsuarioRepository usuarioRepository,
            PublicacionRepository publicacionRepository,
            ComentarioRepository comentarioRepository,
            BaneoRepository baneoRepository,
            EjercicioRepository ejercicioRepository,
            UserInfoMapper userInfoMapper) {
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.comentarioRepository = comentarioRepository;
        this.baneoRepository = baneoRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.userInfoMapper = userInfoMapper;
    }



    @Transactional(readOnly = true)
    public Page<UsuarioDto> listUsers(String q, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir, USER_SORT, "fechaCreacion");
        if (q == null || q.isBlank()) {
            return usuarioRepository.findAll(pageable).map(userInfoMapper::toUsuarioDto);
        }
        String search = q.trim();
        List<UsuarioEntity> entities = usuarioRepository.findBySearchPaged(search, size, (long) page * size);
        long total = usuarioRepository.countBySearch(search);
        return new PageImpl<>(entities.stream().map(userInfoMapper::toUsuarioDto).toList(), pageable, total);
    }

    @Transactional
    public UsuarioDto updateUser(Integer id, AdminUserUpdateRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByIdWithBaneos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        if (request.rol() != null) {
            usuario.setRol(request.rol());
        }
        if (request.estaActivo() != null) {
            usuario.setEstaActivo(request.estaActivo());
        }
        return userInfoMapper.toUsuarioDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deleteUser(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }



    @Transactional(readOnly = true)
    public Page<AdminPostDto> listPosts(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir, POST_SORT, "fechaCreacion");
        return publicacionRepository.findAll(pageable).map(this::toAdminPostDto);
    }

    @Transactional
    public AdminPostDto togglePostActiva(Integer id) {
        PublicacionEntity p = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada: " + id));
        p.setEstaActiva(!p.isEstaActiva());
        return toAdminPostDto(publicacionRepository.save(p));
    }

    @Transactional
    public void deletePost(Integer id) {
        if (!publicacionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Publicación no encontrada: " + id);
        }
        publicacionRepository.deleteById(id);
    }



    @Transactional(readOnly = true)
    public Page<AdminCommentDto> listComments(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir, COMMENT_SORT, "fechaCreacion");
        return comentarioRepository.findAll(pageable).map(this::toAdminCommentDto);
    }

    @Transactional
    public void deleteComment(Integer id) {
        if (!comentarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comentario no encontrado: " + id);
        }
        comentarioRepository.deleteById(id);
    }



    @Transactional(readOnly = true)
    public Page<BaneoDto> listBans(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir, BAN_SORT, "fechaCreacion");
        return baneoRepository.findAll(pageable).map(userInfoMapper::toBaneoDto);
    }

    @Transactional
    public BaneoDto createBan(AdminBanCreateRequest request) {
        UsuarioEntity usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        boolean hasActiveBan = !baneoRepository
                .findActiveBans(request.usuarioId(), Instant.now(), PageRequest.of(0, 1))
                .isEmpty();
        if (hasActiveBan) {
            throw new ResourceConflictException("El usuario ya tiene un baneo activo. Elimínalo antes de crear uno nuevo.");
        }

        BaneoEntity ban = new BaneoEntity();
        ban.setUsuario(usuario);
        ban.setMotivoBaneo(request.motivoBaneo());
        ban.setBaneadoHasta(request.baneadoHasta());
        ban.setBaneadoPermanentemente(request.baneadoPermanentemente());
        ban.setFechaCreacion(Instant.now());
        return userInfoMapper.toBaneoDto(baneoRepository.save(ban));
    }

    @Transactional
    public BaneoDto updateBan(Integer id, AdminBanUpdateRequest request) {
        BaneoEntity ban = baneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Baneo no encontrado: " + id));
        ban.setMotivoBaneo(request.motivoBaneo());
        ban.setBaneadoPermanentemente(request.baneadoPermanentemente());
        ban.setBaneadoHasta(request.baneadoPermanentemente() ? null : request.baneadoHasta());
        return userInfoMapper.toBaneoDto(baneoRepository.save(ban));
    }

    @Transactional
    public void deleteBan(Integer id) {
        if (!baneoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Baneo no encontrado: " + id);
        }
        baneoRepository.deleteById(id);
    }



    @Transactional(readOnly = true)
    public Page<AdminEjercicioDto> listEjercicios(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir, EJ_SORT, "nombre");
        return ejercicioRepository.findAll(pageable).map(this::toAdminEjercicioDto);
    }

    @Transactional
    public AdminEjercicioDto createEjercicio(AdminEjercicioRequest request) {
        EjercicioEntity e = new EjercicioEntity();
        e.setNombre(request.nombre().trim());
        e.setDescripcion(request.descripcion());
        e.setImagenUrl(request.imagenUrl());
        e.setFechaCreacion(Instant.now());
        return toAdminEjercicioDto(ejercicioRepository.save(e));
    }

    @Transactional
    public AdminEjercicioDto updateEjercicio(Integer id, AdminEjercicioRequest request) {
        EjercicioEntity e = ejercicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado: " + id));
        e.setNombre(request.nombre().trim());
        e.setDescripcion(request.descripcion());
        e.setImagenUrl(request.imagenUrl());
        return toAdminEjercicioDto(ejercicioRepository.save(e));
    }

    @Transactional
    public void deleteEjercicio(Integer id) {
        if (!ejercicioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ejercicio no encontrado: " + id);
        }
        ejercicioRepository.deleteById(id);
    }



    private Pageable buildPageable(int page, int size, String sortBy, String sortDir, Set<String> allowed, String defaultField) {
        String field = (sortBy != null && allowed.contains(sortBy)) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, field));
    }

    private AdminPostDto toAdminPostDto(PublicacionEntity p) {
        return new AdminPostDto(
                p.getId(),
                p.getPublicId(),
                p.getUsuario().getUsername(),
                p.getTitulo(),
                p.getContadorLikes(),
                p.getContadorComentarios(),
                p.getMultimediaUrl(),
                p.isEstaActiva(),
                p.getFechaCreacion()
        );
    }

    private AdminCommentDto toAdminCommentDto(ComentarioEntity c) {
        String username = c.getUsuario() != null ? c.getUsuario().getUsername() : null;
        return new AdminCommentDto(
                c.getId(),
                username,
                c.getTexto(),
                c.getPublicacion().getId(),
                c.isEstaActivo(),
                c.getContadorLikes(),
                c.getFechaCreacion()
        );
    }

    private AdminEjercicioDto toAdminEjercicioDto(EjercicioEntity e) {
        return new AdminEjercicioDto(
                e.getId(),
                e.getNombre(),
                e.getDescripcion(),
                e.getImagenUrl(),
                e.getFechaCreacion()
        );
    }
}
