package com.agg.dumbellcheck.security;

import com.agg.dumbellcheck.dto.ApiErrorResponse;
import com.agg.dumbellcheck.entities.BaneoEntity;
import com.agg.dumbellcheck.exceptions.UserBannedException;
import com.agg.dumbellcheck.repositories.BaneoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Component
public class BanCheckFilter extends OncePerRequestFilter {

    private final BaneoRepository baneoRepository;
    private final ObjectMapper objectMapper;

    public BanCheckFilter(BaneoRepository baneoRepository, ObjectMapper objectMapper) {
        this.baneoRepository = baneoRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<BaneoEntity> activeBan =
                    baneoRepository.findActiveBanByUsername(auth.getName(), Instant.now());

            if (activeBan.isPresent()) {
                BaneoEntity ban = activeBan.get();
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                objectMapper.writeValue(response.getWriter(),
                        ApiErrorResponse.of(
                                "Tu cuenta está suspendida.",
                                "USER_BANNED",
                                new UserBannedException.BanInfo(
                                        ban.getMotivoBaneo(),
                                        ban.getBaneadoHasta(),
                                        ban.isBaneadoPermanentemente())));
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
