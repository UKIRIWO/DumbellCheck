package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.ComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<ComentarioEntity, Integer> {

    @Query("""
            SELECT c FROM ComentarioEntity c
            JOIN FETCH c.usuario
            WHERE c.publicacion.id = :publicacionId
              AND c.estaActivo = true
              AND c.comentarioPadre IS NULL
            ORDER BY c.fechaCreacion ASC, c.id ASC
            """)
    List<ComentarioEntity> findActiveByPublicacionId(@Param("publicacionId") Integer publicacionId);
}
