package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.UserInfoDTO.*;
import com.agg.dumbellcheck.services.PerfilService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/usuarios")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/{username}/perfil")
    public ApiSuccessResponse<PerfilDto> getPerfil(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(perfilService.getPerfil(userDetails.getUsername(), username));
    }

    @PutMapping("/me/perfil")
    public ApiSuccessResponse<PerfilDto> updateMyProfile(
            @Valid @RequestBody PerfilUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(perfilService.updateMyProfile(userDetails.getUsername(), request));
    }

    @PostMapping("/me/foto")
    public ApiSuccessResponse<String> uploadFoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(perfilService.uploadFotoPerfil(userDetails.getUsername(), file));
    }

    @PostMapping("/me/banner")
    public ApiSuccessResponse<String> uploadBanner(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(perfilService.uploadBanner(userDetails.getUsername(), file));
    }

    @PostMapping("/me/enlaces")
    public ApiSuccessResponse<UsuarioEnlaceDto> addEnlace(
            @Valid @RequestBody UsuarioEnlaceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(perfilService.addEnlace(userDetails.getUsername(), request));
    }

    @DeleteMapping("/me/enlaces/{id}")
    public ApiSuccessResponse<Void> deleteEnlace(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        perfilService.deleteEnlace(userDetails.getUsername(), id);
        return ApiSuccessResponse.of(null);
    }
}
