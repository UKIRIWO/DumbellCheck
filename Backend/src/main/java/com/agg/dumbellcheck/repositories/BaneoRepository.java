package com.agg.dumbellcheck.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.agg.dumbellcheck.entities.BaneoEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BaneoRepository extends JpaRepository<BaneoEntity, Integer> {

    List<BaneoEntity> findByUsuario_IdOrderByFechaCreacionDesc(Integer usuarioId);

    Page<BaneoEntity> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    @Query("""
            SELECT b FROM BaneoEntity b
            WHERE b.usuario.id = :userId
              AND (b.baneadoPermanentemente = true OR b.baneadoHasta > :now)
            ORDER BY b.fechaCreacion DESC
            """)
    List<BaneoEntity> findActiveBans(@Param("userId") Integer userId, @Param("now") Instant now, Pageable pageable);


    @Query(value = """
            SELECT b.* FROM baneos b
            JOIN usuarios u ON u.id = b.usuario_id
            WHERE u.username = :username
              AND (b.baneado_permanentemente = 1 OR b.baneado_hasta > :now)
            ORDER BY b.fecha_creacion DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<BaneoEntity> findActiveBanByUsername(@Param("username") String username, @Param("now") Instant now);
}
