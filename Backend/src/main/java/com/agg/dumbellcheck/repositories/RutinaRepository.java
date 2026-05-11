package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.RutinaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RutinaRepository extends JpaRepository<RutinaEntity, Integer> {

    Optional<RutinaEntity> findByPublicId(String publicId);

    @Query("""
            SELECT r FROM RutinaEntity r
            JOIN FETCH r.usuario
            WHERE r.usuario.id = :usuarioId
            ORDER BY r.fechaCreacion DESC
            """)
    List<RutinaEntity> findByUsuarioIdOrderByFechaCreacionDesc(@Param("usuarioId") Integer usuarioId);

    @Query("""
            SELECT r FROM RutinaEntity r
            JOIN FETCH r.usuario
            WHERE r.esPublica = true
            ORDER BY r.fechaCreacion DESC
            """)
    List<RutinaEntity> findAllPublicasOrderByFechaCreacionDesc();
}
