package com.agg.dumbellcheck.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.UserInfoDTO.SidebarDataDto;
import com.agg.dumbellcheck.dto.UserInfoDTO.SidebarSuggestionDto;
import com.agg.dumbellcheck.dto.UserInfoDTO.UsuarioDto;
import com.agg.dumbellcheck.services.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    public ApiSuccessResponse<UsuarioDto> getById(@PathVariable Integer id) {
        return ApiSuccessResponse.of(usuarioService.getUsuarioById(id));
    }

    @GetMapping("/me/sidebar")
    public ApiSuccessResponse<SidebarDataDto> getSidebarData(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "6") int limit) {
        return ApiSuccessResponse.of(usuarioService.getSidebarData(userDetails.getUsername(), Math.min(limit, 20)));
    }

    @GetMapping("/search")
    public ApiSuccessResponse<List<SidebarSuggestionDto>> searchUsuarios(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiSuccessResponse.of(usuarioService.searchUsuarios(query, limit));
    }
}
