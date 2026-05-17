package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.AdminDTO.*;
import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.UserInfoDTO.BaneoDto;
import com.agg.dumbellcheck.dto.UserInfoDTO.UsuarioDto;
import com.agg.dumbellcheck.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/usuarios")
    public ApiSuccessResponse<Page<UsuarioDto>> listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiSuccessResponse.of(adminService.listUsers(q, page, Math.min(size, 100), sortBy, sortDir));
    }

    @PutMapping("/usuarios/{id}")
    public ApiSuccessResponse<UsuarioDto> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        return ApiSuccessResponse.of(adminService.updateUser(id, request));
    }

    @DeleteMapping("/usuarios/{id}")
    public ApiSuccessResponse<Void> deleteUser(@PathVariable Integer id) {
        adminService.deleteUser(id);
        return ApiSuccessResponse.of(null);
    }

    @GetMapping("/publicaciones")
    public ApiSuccessResponse<Page<AdminPostDto>> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiSuccessResponse.of(adminService.listPosts(page, Math.min(size, 100), sortBy, sortDir));
    }

    @PutMapping("/publicaciones/{id}/estado")
    public ApiSuccessResponse<AdminPostDto> togglePostActiva(@PathVariable Integer id) {
        return ApiSuccessResponse.of(adminService.togglePostActiva(id));
    }

    @DeleteMapping("/publicaciones/{id}")
    public ApiSuccessResponse<Void> deletePost(@PathVariable Integer id) {
        adminService.deletePost(id);
        return ApiSuccessResponse.of(null);
    }

    @GetMapping("/comentarios")
    public ApiSuccessResponse<Page<AdminCommentDto>> listComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiSuccessResponse.of(adminService.listComments(page, Math.min(size, 100), sortBy, sortDir));
    }

    @DeleteMapping("/comentarios/{id}")
    public ApiSuccessResponse<Void> deleteComment(@PathVariable Integer id) {
        adminService.deleteComment(id);
        return ApiSuccessResponse.of(null);
    }

    @GetMapping("/baneos")
    public ApiSuccessResponse<Page<BaneoDto>> listBans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiSuccessResponse.of(adminService.listBans(page, Math.min(size, 100), sortBy, sortDir));
    }

    @PostMapping("/baneos")
    public ApiSuccessResponse<BaneoDto> createBan(@Valid @RequestBody AdminBanCreateRequest request) {
        return ApiSuccessResponse.of(adminService.createBan(request));
    }

    @PutMapping("/baneos/{id}")
    public ApiSuccessResponse<BaneoDto> updateBan(
            @PathVariable Integer id,
            @Valid @RequestBody AdminBanUpdateRequest request) {
        return ApiSuccessResponse.of(adminService.updateBan(id, request));
    }

    @DeleteMapping("/baneos/{id}")
    public ApiSuccessResponse<Void> deleteBan(@PathVariable Integer id) {
        adminService.deleteBan(id);
        return ApiSuccessResponse.of(null);
    }

    @GetMapping("/ejercicios")
    public ApiSuccessResponse<Page<AdminEjercicioDto>> listEjercicios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ApiSuccessResponse.of(adminService.listEjercicios(page, Math.min(size, 100), sortBy, sortDir));
    }

    @PostMapping("/ejercicios")
    public ApiSuccessResponse<AdminEjercicioDto> createEjercicio(@Valid @RequestBody AdminEjercicioRequest request) {
        return ApiSuccessResponse.of(adminService.createEjercicio(request));
    }

    @PutMapping("/ejercicios/{id}")
    public ApiSuccessResponse<AdminEjercicioDto> updateEjercicio(
            @PathVariable Integer id,
            @Valid @RequestBody AdminEjercicioRequest request) {
        return ApiSuccessResponse.of(adminService.updateEjercicio(id, request));
    }

    @DeleteMapping("/ejercicios/{id}")
    public ApiSuccessResponse<Void> deleteEjercicio(@PathVariable Integer id) {
        adminService.deleteEjercicio(id);
        return ApiSuccessResponse.of(null);
    }
}
