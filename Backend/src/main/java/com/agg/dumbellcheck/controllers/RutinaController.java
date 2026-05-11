package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.RutinaCreateRequest;
import com.agg.dumbellcheck.dto.RutinaListItemResponse;
import com.agg.dumbellcheck.dto.RutinaResponse;
import com.agg.dumbellcheck.services.RutinaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutinas")
public class RutinaController {

    private final RutinaService rutinaService;

    public RutinaController(RutinaService rutinaService) {
        this.rutinaService = rutinaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiSuccessResponse<RutinaResponse> createRutina(
            @Valid @RequestBody RutinaCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(rutinaService.createRutina(userDetails.getUsername(), request));
    }

    @GetMapping("/mias")
    public ApiSuccessResponse<List<RutinaListItemResponse>> getMyRutinas(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(rutinaService.getMyRutinas(userDetails.getUsername()));
    }

    @GetMapping
    public ApiSuccessResponse<List<RutinaListItemResponse>> getPublicRutinas() {
        return ApiSuccessResponse.of(rutinaService.getPublicRutinas());
    }

    @GetMapping("/{publicId}")
    public ApiSuccessResponse<RutinaResponse> getRutina(@PathVariable String publicId) {
        return ApiSuccessResponse.of(rutinaService.getRutinaByPublicId(publicId));
    }

    @PutMapping("/{publicId}")
    public ApiSuccessResponse<RutinaResponse> updateRutina(
            @PathVariable String publicId,
            @Valid @RequestBody RutinaCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(rutinaService.updateRutina(publicId, userDetails.getUsername(), request));
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRutina(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        rutinaService.deleteRutina(publicId, userDetails.getUsername());
    }
}
