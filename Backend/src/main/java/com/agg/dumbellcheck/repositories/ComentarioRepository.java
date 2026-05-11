package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.ComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComentarioRepository extends JpaRepository<ComentarioEntity, Integer> {

    @Query("""
            SELECT c FROM ComentarioEntity c
            JOIN FETCH c.usuario
            LEFT JOIN FETCH c.comentarioPadre
            WHERE c.publicacion.id = :publicacionId
              AND c.estaActivo = true
            ORDER BY c.fechaCreacion ASC, c.id ASC
            """)
    List<ComentarioEntity> findActiveByPublicacionId(@Param("publicacionId") Integer publicacionId);

    Optional<ComentarioEntity> findByIdAndPublicacionIdAndEstaActivoTrue(Integer id, Integer publicacionId);
}
