package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.AuthLoginRequest;
import com.agg.dumbellcheck.dto.AuthLoginResponse;
import com.agg.dumbellcheck.dto.AuthRegisterRequest;
import com.agg.dumbellcheck.dto.UserInfoDTO.UsuarioDto;
import com.agg.dumbellcheck.services.AuthService;
import com.agg.dumbellcheck.services.GoogleAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    public AuthController(AuthService authService, GoogleAuthService googleAuthService) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/register")
    public ApiSuccessResponse<UsuarioDto> register(@Valid @RequestBody AuthRegisterRequest request) {
        return ApiSuccessResponse.of(authService.register(request));
    }

    @PostMapping("/login")
    public ApiSuccessResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ApiSuccessResponse.of(authService.login(request));
    }

    @GetMapping("/google/start")
    public ResponseEntity<Void> startGoogleLogin() {
        String state = googleAuthService.generateState();
        ResponseCookie stateCookie = ResponseCookie.from("dc_google_oauth_state", state)
                .httpOnly(true)
                .secure(googleAuthService.useSecureStateCookie())
                .sameSite("Lax")
                .path("/")
                .maxAge(300)
                .build();

        String redirectUrl = googleAuthService.buildAuthorizationUrl(state);
        return ResponseEntity.status(302)
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @CookieValue(name = "dc_google_oauth_state", required = false) String stateFromCookie
    ) {
        ResponseCookie clearCookie = ResponseCookie.from("dc_google_oauth_state", "")
                .httpOnly(true)
                .secure(googleAuthService.useSecureStateCookie())
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        if (error != null && !error.isBlank()) {
            return redirectWithClearCookie(googleAuthService.buildFrontendErrorRedirect("Inicio con Google cancelado"), clearCookie);
        }

        if (code == null || code.isBlank() || state == null || stateFromCookie == null || !state.equals(stateFromCookie)) {
            return redirectWithClearCookie(googleAuthService.buildFrontendErrorRedirect("State OAuth invalido"), clearCookie);
        }

        try {
            AuthLoginResponse session = googleAuthService.loginOrRegisterFromGoogleCode(code);
            return redirectWithClearCookie(googleAuthService.buildFrontendSuccessRedirect(session), clearCookie);
        } catch (RuntimeException ex) {
            return redirectWithClearCookie(googleAuthService.buildFrontendErrorRedirect(ex.getMessage()), clearCookie);
        }
    }

    private ResponseEntity<Void> redirectWithClearCookie(String location, ResponseCookie clearCookie) {
        return ResponseEntity.status(302)
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .header(HttpHeaders.LOCATION, location)
                .build();
    }
}
