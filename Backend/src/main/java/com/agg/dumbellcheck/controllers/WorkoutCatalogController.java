package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.WorkoutInfoDTO.EjercicioDto;
import com.agg.dumbellcheck.dto.WorkoutInfoDTO.GrupoMuscularDto;
import com.agg.dumbellcheck.entities.EjercicioEntity;
import com.agg.dumbellcheck.entities.GrupoMuscularEntity;
import com.agg.dumbellcheck.repositories.EjercicioRepository;
import com.agg.dumbellcheck.repositories.GrupoMuscularRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WorkoutCatalogController {

    private final EjercicioRepository ejercicioRepository;
    private final GrupoMuscularRepository grupoMuscularRepository;

    public WorkoutCatalogController(EjercicioRepository ejercicioRepository,
                                    GrupoMuscularRepository grupoMuscularRepository) {
        this.ejercicioRepository = ejercicioRepository;
        this.grupoMuscularRepository = grupoMuscularRepository;
    }

    @GetMapping("/api/ejercicios")
    public ApiSuccessResponse<List<EjercicioDto>> getEjercicios(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer grupoMuscularId) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        List<EjercicioEntity> ejercicios = ejercicioRepository.findByCriteria(normalizedSearch, grupoMuscularId);
        List<EjercicioDto> result = ejercicios.stream()
                .map(e -> new EjercicioDto(e.getId(), e.getNombre(), e.getDescripcion(), e.getImagenUrl(), e.getFechaCreacion()))
                .toList();
        return ApiSuccessResponse.of(result);
    }

    @GetMapping("/api/grupos-musculares")
    public ApiSuccessResponse<List<GrupoMuscularDto>> getGruposMusculares() {
        List<GrupoMuscularEntity> grupos = grupoMuscularRepository.findAll();
        List<GrupoMuscularDto> result = grupos.stream()
                .map(g -> new GrupoMuscularDto(g.getId(), g.getNombre(), g.getDescripcion(), g.getFechaCreacion()))
                .toList();
        return ApiSuccessResponse.of(result);
    }
}
