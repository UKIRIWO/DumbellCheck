package com.agg.dumbellcheck.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.agg.dumbellcheck.entities.UsuarioEntity;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    Optional<UsuarioEntity> findByUsername(String username);
    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM UsuarioEntity u LEFT JOIN FETCH u.baneos WHERE u.id = :id")
    Optional<UsuarioEntity> findByIdWithBaneos(@Param("id") Integer id);

    @Query("SELECT u FROM UsuarioEntity u WHERE u.username = :principal OR LOWER(u.email) = LOWER(:principal)")
    Optional<UsuarioEntity> findByUsernameOrEmail(@Param("principal") String principal);
}
