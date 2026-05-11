package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.DetalleSerieRutinaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetalleSerieRutinaRepository extends JpaRepository<DetalleSerieRutinaEntity, Integer> {

    List<DetalleSerieRutinaEntity> findByEjercicioRutinaIdOrderByNumeroSerie(Integer ejercicioRutinaId);

    @Modifying
    @Query("DELETE FROM DetalleSerieRutinaEntity d WHERE d.ejercicioRutina.id = :ejRutinaId")
    void deleteByEjercicioRutinaId(@Param("ejRutinaId") Integer ejRutinaId);

    @Modifying
    @Query("""
            DELETE FROM DetalleSerieRutinaEntity d
            WHERE d.ejercicioRutina.id IN (
                SELECT e.id FROM EjercicioRutinaEntity e WHERE e.rutina.id = :rutinaId
            )
            """)
    void deleteSeriesByRutinaId(@Param("rutinaId") Integer rutinaId);
}
