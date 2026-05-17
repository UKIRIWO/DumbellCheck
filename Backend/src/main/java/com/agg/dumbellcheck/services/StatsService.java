package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.StatsResponse;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.repositories.StatsRepository;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StatsService {

    private static final Locale LOCALE_ES_ES = Locale.of("es", "ES");

    private final UsuarioRepository usuarioRepository;
    private final StatsRepository statsRepository;

    public StatsService(UsuarioRepository usuarioRepository, StatsRepository statsRepository) {
        this.usuarioRepository = usuarioRepository;
        this.statsRepository = statsRepository;
    }

    @Transactional(readOnly = true)
    public StatsResponse getMyStats(String username, int months) {
        int clampedMonths = Math.max(1, Math.min(3, months));

        Integer userId = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"))
                .getId();

        LocalDate cutoffDate = LocalDate.now(ZoneOffset.UTC).minusMonths(clampedMonths);
        Instant cutoff = cutoffDate.atStartOfDay(ZoneOffset.UTC).toInstant();

        int totalWorkouts = (int) statsRepository.countWorkoutsSince(userId, cutoff);
        Object[] volumeRow = statsRepository.fetchVolumeSummary(userId, cutoff);
        double totalVolumenKg = volumeRow[0] != null ? ((Number) volumeRow[0]).doubleValue() : 0.0;
        int totalSeries = volumeRow[1] != null ? ((Number) volumeRow[1]).intValue() : 0;
        int totalRepeticiones = volumeRow[2] != null ? ((Number) volumeRow[2]).intValue() : 0;

        List<StatsResponse.WeeklyActivity> actividadSemanal = buildWeeklyActivity(userId, cutoffDate, cutoff);
        List<StatsResponse.VolumeByGroup> volumenPorGrupo = mapVolumeByGroup(
                statsRepository.fetchVolumeByMuscleGroup(userId, cutoff));
        List<StatsResponse.ExerciseFrequency> ejerciciosMasFrecuentes = mapExerciseFrequency(
                statsRepository.fetchExerciseFrequencyTop8(userId, cutoff));
        List<StatsResponse.ExerciseBestLift> mejoresMarcas = mapBestLifts(
                statsRepository.fetchBestLiftsTop8(userId, cutoff));

        return new StatsResponse(
                totalWorkouts,
                Math.round(totalVolumenKg * 10.0) / 10.0,
                totalSeries,
                totalRepeticiones,
                actividadSemanal,
                volumenPorGrupo,
                ejerciciosMasFrecuentes,
                mejoresMarcas
        );
    }

    private List<StatsResponse.WeeklyActivity> buildWeeklyActivity(Integer userId, LocalDate cutoffDate, Instant cutoff) {
        List<Object[]> rows = statsRepository.fetchWeeklyPublicationCounts(userId, cutoff);

        Map<Integer, Integer> countByYearWeek = new HashMap<>();
        for (Object[] row : rows) {
            countByYearWeek.put(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
        }

        List<StatsResponse.WeeklyActivity> result = new ArrayList<>();
        LocalDate weekStart = cutoffDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        WeekFields iso = WeekFields.ISO;
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("d MMM", LOCALE_ES_ES);

        int prevMonth = -1;
        while (!weekStart.isAfter(today)) {
            int yearWeekKey = weekStart.get(iso.weekBasedYear()) * 100
                    + weekStart.get(iso.weekOfWeekBasedYear());
            int count = countByYearWeek.getOrDefault(yearWeekKey, 0);
            boolean isNewMonth = weekStart.getMonthValue() != prevMonth;
            result.add(new StatsResponse.WeeklyActivity(weekStart.format(labelFmt), isNewMonth, count));
            prevMonth = weekStart.getMonthValue();
            weekStart = weekStart.plusWeeks(1);
        }
        return result;
    }

    private List<StatsResponse.VolumeByGroup> mapVolumeByGroup(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new StatsResponse.VolumeByGroup(
                        (String) r[0],
                        Math.round(((Number) r[1]).doubleValue() * 10.0) / 10.0))
                .toList();
    }

    private List<StatsResponse.ExerciseFrequency> mapExerciseFrequency(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new StatsResponse.ExerciseFrequency(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).intValue()))
                .toList();
    }

    private List<StatsResponse.ExerciseBestLift> mapBestLifts(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new StatsResponse.ExerciseBestLift(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).doubleValue()))
                .toList();
    }
}
