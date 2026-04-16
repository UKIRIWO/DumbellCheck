package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.AuthLoginRequest;
import com.agg.dumbellcheck.dto.AuthLoginResponse;
import com.agg.dumbellcheck.dto.AuthRegisterRequest;
import com.agg.dumbellcheck.dto.UserInfoDTO.UsuarioDto;
import com.agg.dumbellcheck.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiSuccessResponse<UsuarioDto> register(@Valid @RequestBody AuthRegisterRequest request) {
        return ApiSuccessResponse.of(authService.register(request));
    }

    @PostMapping("/login")
    public ApiSuccessResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ApiSuccessResponse.of(authService.login(request));
    }
}
