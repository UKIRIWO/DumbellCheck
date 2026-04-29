package com.agg.dumbellcheck.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.agg.dumbellcheck.entities.UsuarioEntity;

import java.util.Optional;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    Optional<UsuarioEntity> findByUsername(String username);
    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM UsuarioEntity u LEFT JOIN FETCH u.baneos WHERE u.id = :id")
    Optional<UsuarioEntity> findByIdWithBaneos(@Param("id") Integer id);

    @Query("SELECT u FROM UsuarioEntity u WHERE u.username = :principal OR LOWER(u.email) = LOWER(:principal)")
    Optional<UsuarioEntity> findByUsernameOrEmail(@Param("principal") String principal);

    @Query(value = """
            SELECT u.* FROM usuarios u
            JOIN (
                SELECT s2.seguido_id, COUNT(*) as common_count
                FROM seguidores s1
                JOIN seguidores s2 ON s1.seguido_id = s2.usuario_id
                WHERE s1.usuario_id = :userId
                  AND s2.seguido_id != :userId
                  AND s2.seguido_id NOT IN (
                      SELECT seguido_id FROM seguidores WHERE usuario_id = :userId
                  )
                GROUP BY s2.seguido_id
                ORDER BY common_count DESC
                LIMIT :limit
            ) as suggestions ON u.id = suggestions.seguido_id
            """, nativeQuery = true)
    List<UsuarioEntity> findSuggestedUsers(@Param("userId") Integer userId, @Param("limit") int limit);
}
