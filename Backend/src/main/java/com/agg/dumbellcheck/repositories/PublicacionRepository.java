package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.PublicacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<PublicacionEntity, Integer> {

    Page<PublicacionEntity> findByEstaActivaTrueOrderByFechaCreacionDesc(Pageable pageable);

    Optional<PublicacionEntity> findByIdAndEstaActivaTrue(Integer id);

    // Cursor-based pagination for user profile (first page — no cursor)
    List<PublicacionEntity> findByUsuarioIdAndEstaActivaTrueOrderByIdDesc(Integer usuarioId, Pageable pageable);

    // Cursor-based pagination for user profile (subsequent pages — cursor = last seen id)
    List<PublicacionEntity> findByUsuarioIdAndEstaActivaTrueAndIdLessThanOrderByIdDesc(
            Integer usuarioId, Integer cursorId, Pageable pageable);
}
