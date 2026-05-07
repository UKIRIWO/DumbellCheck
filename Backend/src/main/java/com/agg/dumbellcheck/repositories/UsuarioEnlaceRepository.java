package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.UsuarioEnlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioEnlaceRepository extends JpaRepository<UsuarioEnlaceEntity, Integer> {

    List<UsuarioEnlaceEntity> findByUsuarioIdOrderByOrdenAsc(Integer usuarioId);

    long countByUsuarioId(Integer usuarioId);

    boolean existsByUsuarioIdAndUrl(Integer usuarioId, String url);
}
