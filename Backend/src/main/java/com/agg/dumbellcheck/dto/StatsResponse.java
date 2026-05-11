package com.agg.dumbellcheck.dto;

import java.util.List;

public record StatsResponse(
        int totalWorkouts,
        double totalVolumenKg,
        int totalSeries,
        int totalRepeticiones,
        List<WeeklyActivity> actividadSemanal,
        List<VolumeByGroup> volumenPorGrupo,
        List<ExerciseFrequency> ejerciciosMasFrecuentes,
        List<ExerciseBestLift> mejoresMarcas
) {
    public record WeeklyActivity(String semana, boolean isNewMonth, int cantidad) {}
    public record VolumeByGroup(String grupo, double volumenKg) {}
    public record ExerciseFrequency(String ejercicio, String imagenUrl, int veces) {}
    public record ExerciseBestLift(String ejercicio, String imagenUrl, double maxPesoKg) {}
}
