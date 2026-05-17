package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.PublicacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<PublicacionEntity, Integer> {

    Page<PublicacionEntity> findByEstaActivaTrueOrderByFechaCreacionDesc(Pageable pageable);

    Optional<PublicacionEntity> findByIdAndEstaActivaTrue(Integer id);

    Optional<PublicacionEntity> findByPublicIdAndEstaActivaTrue(String publicId);


    List<PublicacionEntity> findByUsuarioIdAndEstaActivaTrueOrderByIdDesc(Integer usuarioId, Pageable pageable);


    List<PublicacionEntity> findByUsuarioIdAndEstaActivaTrueAndIdLessThanOrderByIdDesc(
            Integer usuarioId, Integer cursorId, Pageable pageable);

    @Query("""
            SELECT p FROM PublicacionEntity p
            WHERE p.estaActiva = true
              AND p.usuario.id <> :userId
              AND p.usuario.id IN (
                  SELECT s.seguido.id FROM SeguidorEntity s WHERE s.usuario.id = :userId
              )
            ORDER BY p.fechaCreacion DESC
            """)
    Page<PublicacionEntity> findFeedAmigos(@Param("userId") Integer userId, Pageable pageable);

    @Query("""
            SELECT p FROM PublicacionEntity p
            WHERE p.estaActiva = true
              AND p.usuario.id <> :userId
              AND p.usuario.id NOT IN (
                  SELECT s.seguido.id FROM SeguidorEntity s WHERE s.usuario.id = :userId
              )
            ORDER BY p.fechaCreacion DESC
            """)
    Page<PublicacionEntity> findFeedDescubrir(@Param("userId") Integer userId, Pageable pageable);
}
