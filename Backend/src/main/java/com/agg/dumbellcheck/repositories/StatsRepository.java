package com.agg.dumbellcheck.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class StatsRepository {

    @PersistenceContext
    private EntityManager em;

    public long countWorkoutsSince(Integer userId, Instant cutoff) {
        Object result = em.createNativeQuery(
                        "SELECT COUNT(*) FROM publicaciones "
                                + "WHERE usuario_id = :uid AND esta_activa = 1 AND fecha_creacion >= :cutoff")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getSingleResult();
        return ((Number) result).longValue();
    }

    public Object[] fetchVolumeSummary(Integer userId, Instant cutoff) {
        Object row = em.createNativeQuery(
                        "SELECT COALESCE(SUM(ds.repeticiones * ds.peso), 0), "
                                + "       COUNT(ds.id), "
                                + "       COALESCE(SUM(ds.repeticiones), 0) "
                                + "FROM publicaciones p "
                                + "JOIN ejercicios_publicacion ep ON ep.publicacion_id = p.id "
                                + "JOIN detalles_series ds ON ds.ejercicio_publicacion_id = ep.id "
                                + "WHERE p.usuario_id = :uid AND p.esta_activa = 1 AND p.fecha_creacion >= :cutoff")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getSingleResult();
        return (Object[]) row;
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> fetchWeeklyPublicationCounts(Integer userId, Instant cutoff) {
        return em.createNativeQuery(
                        "SELECT YEARWEEK(fecha_creacion, 3) as yw, COUNT(*) as cnt "
                                + "FROM publicaciones "
                                + "WHERE usuario_id = :uid AND esta_activa = 1 AND fecha_creacion >= :cutoff "
                                + "GROUP BY yw ORDER BY yw ASC")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> fetchVolumeByMuscleGroup(Integer userId, Instant cutoff) {
        return em.createNativeQuery(
                        "SELECT gm.nombre, COALESCE(SUM(ds.repeticiones * ds.peso), 0) as volumen "
                                + "FROM publicaciones p "
                                + "JOIN ejercicios_publicacion ep ON ep.publicacion_id = p.id "
                                + "JOIN ejercicios e ON e.id = ep.ejercicio_id "
                                + "JOIN ejercicios_grupos_musculares egm ON egm.ejercicio_id = e.id "
                                + "JOIN grupos_musculares gm ON gm.id = egm.grupo_muscular_id "
                                + "JOIN detalles_series ds ON ds.ejercicio_publicacion_id = ep.id "
                                + "WHERE p.usuario_id = :uid AND p.esta_activa = 1 AND p.fecha_creacion >= :cutoff "
                                + "GROUP BY gm.id, gm.nombre "
                                + "ORDER BY volumen DESC")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> fetchExerciseFrequencyTop8(Integer userId, Instant cutoff) {
        return em.createNativeQuery(
                        "SELECT e.nombre, e.imagen_url, COUNT(ep.id) as veces "
                                + "FROM publicaciones p "
                                + "JOIN ejercicios_publicacion ep ON ep.publicacion_id = p.id "
                                + "JOIN ejercicios e ON e.id = ep.ejercicio_id "
                                + "WHERE p.usuario_id = :uid AND p.esta_activa = 1 AND p.fecha_creacion >= :cutoff "
                                + "GROUP BY e.id, e.nombre, e.imagen_url "
                                + "ORDER BY veces DESC "
                                + "LIMIT 8")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> fetchBestLiftsTop8(Integer userId, Instant cutoff) {
        return em.createNativeQuery(
                        "SELECT e.nombre, e.imagen_url, MAX(ds.peso) as maxPeso "
                                + "FROM publicaciones p "
                                + "JOIN ejercicios_publicacion ep ON ep.publicacion_id = p.id "
                                + "JOIN ejercicios e ON e.id = ep.ejercicio_id "
                                + "JOIN detalles_series ds ON ds.ejercicio_publicacion_id = ep.id "
                                + "WHERE p.usuario_id = :uid AND p.esta_activa = 1 AND p.fecha_creacion >= :cutoff "
                                + "GROUP BY e.id, e.nombre, e.imagen_url "
                                + "ORDER BY maxPeso DESC "
                                + "LIMIT 8")
                .setParameter("uid", userId)
                .setParameter("cutoff", cutoff)
                .getResultList();
    }
}
