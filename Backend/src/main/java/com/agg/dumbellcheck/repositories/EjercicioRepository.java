package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.EjercicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EjercicioRepository extends JpaRepository<EjercicioEntity, Integer> {

    @Query("SELECT DISTINCT e FROM EjercicioEntity e " +
           "LEFT JOIN e.gruposMusculares egm " +
           "WHERE (:search IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:grupoMuscularId IS NULL OR egm.grupoMuscular.id = :grupoMuscularId) " +
           "ORDER BY e.nombre ASC")
    List<EjercicioEntity> findByCriteria(@Param("search") String search,
                                         @Param("grupoMuscularId") Integer grupoMuscularId);
}
