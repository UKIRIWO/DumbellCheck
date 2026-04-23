package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.config.GoogleAuthProperties;
import com.agg.dumbellcheck.dto.AuthLoginResponse;
import com.agg.dumbellcheck.entities.AuthIdentityEntity;
import com.agg.dumbellcheck.entities.AuthProvider;
import com.agg.dumbellcheck.entities.RolUsuario;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import com.agg.dumbellcheck.exceptions.ResourceConflictException;
import com.agg.dumbellcheck.exceptions.UnauthorizedActionException;
import com.agg.dumbellcheck.repositories.AuthIdentityRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final GoogleAuthProperties googleAuthProperties;
    private final AuthIdentityRepository authIdentityRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final RestClient restClient;

    public GoogleAuthService(
            GoogleAuthProperties googleAuthProperties,
            AuthIdentityRepository authIdentityRepository,
            UsuarioRepository usuarioRepository,
            AuthService authService,
            PasswordEncoder passwordEncoder
    ) {
        this.googleAuthProperties = googleAuthProperties;
        this.authIdentityRepository = authIdentityRepository;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.restClient = RestClient.builder().build();
    }

    public String generateState() {
        return UUID.randomUUID().toString();
    }

    public boolean useSecureStateCookie() {
        return googleAuthProperties.secureStateCookie();
    }

    public String buildAuthorizationUrl(String state) {
        return googleAuthProperties.authUri()
                + "?response_type=code"
                + "&client_id=" + encode(googleAuthProperties.clientId())
                + "&redirect_uri=" + encode(googleAuthProperties.redirectUri())
                + "&scope=" + encode(googleAuthProperties.scope())
                + "&state=" + encode(state)
                + "&access_type=offline"
                + "&prompt=consent";
    }

    @Transactional
    public AuthLoginResponse loginOrRegisterFromGoogleCode(String code) {
        GoogleTokenResponse tokenResponse = exchangeCodeForTokens(code);
        GoogleUserInfo userInfo = fetchUserInfo(tokenResponse.accessToken());

        if (!userInfo.emailVerified()) {
            throw new UnauthorizedActionException("Google no confirma que el email este verificado");
        }

        AuthIdentityEntity existingIdentity = authIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, userInfo.sub())
                .orElse(null);

        if (existingIdentity != null) {
            return authService.createSessionForUser(existingIdentity.getUsuario());
        }

        String normalizedEmail = userInfo.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ResourceConflictException("Cuenta existente. Inicia sesion con contrasena para vincular Google.");
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setUsername(generateAvailableUsername(userInfo));
        usuario.setEmail(normalizedEmail);
        usuario.setNombre(firstName(userInfo.name()));
        usuario.setApellido1(lastName(userInfo.name()));
        usuario.setApellido2(null);
        usuario.setFotoPerfilUrl(userInfo.picture());
        usuario.setRol(RolUsuario.MEMBER);
        usuario.setContadorSeguidores(0);
        usuario.setContadorSeguidos(0);
        usuario.setEstaActivo(true);
        usuario.setFechaCreacion(Instant.now());
        usuario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        UsuarioEntity savedUser = usuarioRepository.save(usuario);

        AuthIdentityEntity authIdentity = new AuthIdentityEntity();
        authIdentity.setUsuario(savedUser);
        authIdentity.setProvider(AuthProvider.GOOGLE);
        authIdentity.setProviderUserId(userInfo.sub());
        authIdentity.setEmailProvider(normalizedEmail);
        authIdentity.setEmailVerified(true);
        authIdentity.setCreatedAt(Instant.now());
        authIdentity.setUpdatedAt(Instant.now());
        authIdentityRepository.save(authIdentity);

        return authService.createSessionForUser(savedUser);
    }

    public String buildFrontendSuccessRedirect(AuthLoginResponse session) {
        return googleAuthProperties.frontendSuccessUri()
                + "?accessToken=" + encode(session.accessToken())
                + "&usuarioId=" + session.usuarioId()
                + "&username=" + encode(session.username())
                + "&rol=" + encode(session.rol().name());
    }

    public String buildFrontendErrorRedirect(String message) {
        return googleAuthProperties.frontendErrorUri() + "?googleError=" + encode(message);
    }

    private GoogleTokenResponse exchangeCodeForTokens(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", googleAuthProperties.clientId());
        form.add("client_secret", googleAuthProperties.clientSecret());
        form.add("redirect_uri", googleAuthProperties.redirectUri());
        form.add("grant_type", "authorization_code");

        Map<?, ?> response = restClient.post()
                .uri(googleAuthProperties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        String accessToken = response == null ? null : (String) response.get("access_token");
        if (accessToken == null || accessToken.isBlank()) {
            throw new UnauthorizedActionException("No se pudo obtener el token de Google");
        }

        return new GoogleTokenResponse(accessToken);
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) {
        Map<?, ?> response = restClient.get()
                .uri(googleAuthProperties.userInfoUri())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new UnauthorizedActionException("No se pudo leer el perfil de Google");
        }

        String sub = stringValue(response.get("sub"));
        String email = stringValue(response.get("email"));
        Boolean emailVerified = boolValue(response.get("email_verified"));
        String name = stringValue(response.get("name"));
        String picture = stringValue(response.get("picture"));

        if (sub == null || email == null || emailVerified == null) {
            throw new UnauthorizedActionException("Google no devolvio datos de identidad validos");
        }

        return new GoogleUserInfo(sub, email, emailVerified, name == null ? "Usuario" : name, picture);
    }

    private String generateAvailableUsername(GoogleUserInfo userInfo) {
        String email = userInfo.email().toLowerCase(Locale.ROOT);
        String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String baseUsername = localPart.replaceAll("[^a-z0-9._]", "");
        if (baseUsername.length() < 3) {
            baseUsername = "user";
        }
        if (baseUsername.length() > 30) {
            baseUsername = baseUsername.substring(0, 30);
        }

        String candidate = baseUsername;
        int suffix = 1;
        while (usuarioRepository.existsByUsername(candidate)) {
            candidate = baseUsername + suffix;
            suffix++;
        }
        return candidate;
    }

    private String firstName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isBlank()) {
            return "Usuario";
        }
        String[] parts = trimmed.split("\\s+");
        return parts[0];
    }

    private String lastName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isBlank()) {
            return "Google";
        }
        String[] parts = trimmed.split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : "Google";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String stringValue(Object value) {
        return value instanceof String s ? s : null;
    }

    private Boolean boolValue(Object value) {
        return value instanceof Boolean b ? b : null;
    }

    private record GoogleTokenResponse(String accessToken) {}

    private record GoogleUserInfo(
            String sub,
            String email,
            boolean emailVerified,
            String name,
            String picture
    ) {}
}
