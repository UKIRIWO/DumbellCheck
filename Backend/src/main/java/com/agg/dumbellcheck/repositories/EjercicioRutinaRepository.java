package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.EjercicioRutinaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EjercicioRutinaRepository extends JpaRepository<EjercicioRutinaEntity, Integer> {

    List<EjercicioRutinaEntity> findByRutinaIdOrderByOrden(Integer rutinaId);

    @Modifying
    @Query("DELETE FROM EjercicioRutinaEntity e WHERE e.rutina.id = :rutinaId")
    void deleteByRutinaId(@Param("rutinaId") Integer rutinaId);
}
