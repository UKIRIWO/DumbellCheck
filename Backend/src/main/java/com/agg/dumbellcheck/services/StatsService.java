package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.dto.StatsResponse;
import com.agg.dumbellcheck.exceptions.ResourceNotFoundException;
import com.agg.dumbellcheck.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    private final UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager em;

    public StatsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public StatsResponse getMyStats(String username, int months) {
        int clampedMonths = Math.max(1, Math.min(3, months));

        Integer userId = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"))
                .getId();

        LocalDate cutoffDate = LocalDate.now(ZoneOffset.UTC).minusMonths(clampedMonths);
        Instant cutoff = cutoffDate.atStartOfDay(ZoneOffset.UTC).toInstant();

        int totalWorkouts = countWorkouts(userId, cutoff);
        Object[] volumeRow = queryVolumeSummary(userId, cutoff);
        double totalVolumenKg = volumeRow[0] != null ? ((Number) volumeRow[0]).doubleValue() : 0.0;
        int totalSeries = volumeRow[1] != null ? ((Number) volumeRow[1]).intValue() : 0;
        int totalRepeticiones = volumeRow[2] != null ? ((Number) volumeRow[2]).intValue() : 0;

        List<StatsResponse.WeeklyActivity> actividadSemanal = buildWeeklyActivity(userId, cutoffDate, cutoff);
        List<StatsResponse.VolumeByGroup> volumenPorGrupo = queryVolumeByGroup(userId, cutoff);
        List<StatsResponse.ExerciseFrequency> ejerciciosMasFrecuentes = queryExerciseFrequency(userId, cutoff);
        List<StatsResponse.ExerciseBestLift> mejoresMarcas = queryBestLifts(userId, cutoff);

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

    private int countWorkouts(Integer userId, Instant cutoff) {
        Object result = em.createNativeQuery(
                "SELECT COUNT(*) FROM publicaciones " +
                "WHERE usuario_id = :uid AND esta_activa = 1 AND fecha_creacion >= :cutoff")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getSingleResult();
        return ((Number) result).intValue();
    }

    private Object[] queryVolumeSummary(Integer userId, Instant cutoff) {
        Object row = em.createNativeQuery(
                "SELECT COALESCE(SUM(ds.repeticiones * ds.peso), 0), " +
                "       COUNT(ds.id), " +
                "       COALESCE(SUM(ds.repeticiones), 0) " +
                "FROM publicaciones p " +
                "JOIN ejercicios_publicacion ep ON ep.publicacion_id = p.id " +
                "JOIN detalles_series ds ON ds.ejercicio_publicacion_id = ep.id " +
                "WHERE p.usuario_id = :uid AND p.esta_activa = 1 AND p.fecha_creacion >= :cutoff")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getSingleResult();
        return (Object[]) row;
    }

    @SuppressWarnings("unchecked")
    private List<StatsResponse.WeeklyActivity> buildWeeklyActivity(Integer userId, LocalDate cutoffDate, Instant cutoff) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT YEARWEEK(fecha_creacion, 3) as yw, COUNT(*) as cnt " +
                "FROM publicaciones " +
                "WHERE usuario_id = :uid AND esta_activa = 1 AND fecha_creacion >= :cutoff " +
                "GROUP BY yw ORDER BY yw ASC")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getResultList();

        Map<Integer, Integer> countByYearWeek = new HashMap<>();
        for (Object[] row : rows) {
            countByYearWeek.put(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
        }

        List<StatsResponse.WeeklyActivity> result = new ArrayList<>();
        LocalDate weekStart = cutoffDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        WeekFields iso = WeekFields.ISO;
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("d MMM", new Locale("es", "ES"));

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

    @SuppressWarnings("unchecked")
    private List<StatsResponse.VolumeByGroup> queryVolumeByGroup(Integer userId, Instant cutoff) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT gm.nombre, COALESCE(SUM(ds.repeticiones * ds.peso), 0) as volumen " +
                "FROM publicaciones p " +
                "JOIN ejercicios_publicacion ep ON ep.publicacion_id = p.id " +
                "JOIN ejercicios e ON e.id = ep.ejercicio_id " +
                "JOIN ejercicios_grupos_musculares egm ON egm.ejercicio_id = e.id " +
                "JOIN grupos_musculares gm ON gm.id = egm.grupo_muscular_id " +
                "JOIN detalles_series ds ON ds.ejercicio_publicacion_id = ep.id " +
                "WHERE p.usuario_id = :uid AND p.esta_activa = 1 AND p.fecha_creacion >= :cutoff " +
                "GROUP BY gm.id, gm.nombre " +
                "ORDER BY volumen DESC")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getResultList();

        return rows.stream()
                .map(r -> new StatsResponse.VolumeByGroup(
                        (String) r[0],
                        Math.round(((Number) r[1]).doubleValue() * 10.0) / 10.0))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<StatsResponse.ExerciseFrequency> queryExerciseFrequency(Integer userId, Instant cutoff) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT e.nombre, e.imagen_url, COUNT(ep.id) as veces " +
                "FROM publicaciones p " +
                "JOIN ejercicios_publicacion ep ON ep.publicacion_id = p.id " +
                "JOIN ejercicios e ON e.id = ep.ejercicio_id " +
                "WHERE p.usuario_id = :uid AND p.esta_activa = 1 AND p.fecha_creacion >= :cutoff " +
                "GROUP BY e.id, e.nombre, e.imagen_url " +
                "ORDER BY veces DESC " +
                "LIMIT 8")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getResultList();

        return rows.stream()
                .map(r -> new StatsResponse.ExerciseFrequency(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).intValue()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<StatsResponse.ExerciseBestLift> queryBestLifts(Integer userId, Instant cutoff) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT e.nombre, e.imagen_url, MAX(ds.peso) as maxPeso " +
                "FROM publicaciones p " +
                "JOIN ejercicios_publicacion ep ON ep.publicacion_id = p.id " +
                "JOIN ejercicios e ON e.id = ep.ejercicio_id " +
                "JOIN detalles_series ds ON ds.ejercicio_publicacion_id = ep.id " +
                "WHERE p.usuario_id = :uid AND p.esta_activa = 1 AND p.fecha_creacion >= :cutoff " +
                "GROUP BY e.id, e.nombre, e.imagen_url " +
                "ORDER BY maxPeso DESC " +
                "LIMIT 8")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getResultList();

        return rows.stream()
                .map(r -> new StatsResponse.ExerciseBestLift(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).doubleValue()))
                .toList();
    }
}
