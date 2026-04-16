package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/check")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiSuccessResponse<Map<String, String>> checkAccess() {
        return ApiSuccessResponse.of(Map.of(
                "message", "Acceso de administrador concedido"
        ));
    }
}
