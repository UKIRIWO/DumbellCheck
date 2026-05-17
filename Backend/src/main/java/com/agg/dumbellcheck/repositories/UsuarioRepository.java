package com.agg.dumbellcheck.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.agg.dumbellcheck.entities.UsuarioEntity;

import java.util.Collection;
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
            WHERE u.rol != 'ADMIN'
            """, nativeQuery = true)
    List<UsuarioEntity> findSuggestedUsers(@Param("userId") Integer userId, @Param("limit") int limit);

    @Query("""
            SELECT u FROM UsuarioEntity u
            WHERE u.estaActivo = true
              AND u.rol <> com.agg.dumbellcheck.entities.RolUsuario.ADMIN
              AND (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            ORDER BY u.username ASC
            """)
    List<UsuarioEntity> searchUsuarios(@Param("q") String query, Pageable pageable);

    @Query("""
            SELECT u.username FROM UsuarioEntity u
            WHERE u.estaActivo = true
              AND LOWER(u.username) IN :usernamesLowercase
            """)
    List<String> findExistingUsernames(@Param("usernamesLowercase") Collection<String> usernamesLowercase);


    @Query(value = """
            SELECT u.*,
                   CASE WHEN s.seguido_id IS NOT NULL THEN 0 ELSE 1 END AS sigo_rank
            FROM usuarios u
            LEFT JOIN seguidores s ON s.usuario_id = :myId AND s.seguido_id = u.id
            WHERE u.id != :myId
              AND u.rol != 'ADMIN'
              AND u.esta_activo = 1
              AND (:q IS NULL
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY sigo_rank ASC, u.username ASC
            LIMIT :lim OFFSET :off
            """, nativeQuery = true)
    List<UsuarioEntity> searchUsersForChat(
            @Param("myId") Integer myId,
            @Param("q") String q,
            @Param("lim") int lim,
            @Param("off") long off);


    @Query(value = """
            SELECT * FROM usuarios
            WHERE LOWER(username) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(email) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY
              CASE WHEN LOWER(username) LIKE LOWER(CONCAT('%', :q, '%')) THEN 0 ELSE 1 END,
              fecha_creacion DESC
            LIMIT :lim OFFSET :off
            """, nativeQuery = true)
    List<UsuarioEntity> findBySearchPaged(
            @Param("q") String q,
            @Param("lim") int lim,
            @Param("off") long off);

    @Query(value = """
            SELECT COUNT(*) FROM usuarios
            WHERE LOWER(username) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(email) LIKE LOWER(CONCAT('%', :q, '%'))
            """, nativeQuery = true)
    long countBySearch(@Param("q") String q);
}
