package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.SeguidorEntity;
import com.agg.dumbellcheck.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeguidorRepository extends JpaRepository<SeguidorEntity, Integer> {
    boolean existsByUsuarioIdAndSeguidoId(Integer usuarioId, Integer seguidoId);
    Optional<SeguidorEntity> findByUsuarioIdAndSeguidoId(Integer usuarioId, Integer seguidoId);
    long countByUsuarioId(Integer usuarioId);
    long countBySeguidoId(Integer seguidoId);

    @Query("select s.usuario from SeguidorEntity s where s.seguido.id = :usuarioId")
    List<UsuarioEntity> findSeguidoresUsuariosByUsuarioId(@Param("usuarioId") Integer usuarioId);

    @Query("select s.seguido from SeguidorEntity s where s.usuario.id = :usuarioId")
    List<UsuarioEntity> findSeguidosUsuariosByUsuarioId(@Param("usuarioId") Integer usuarioId);

    @Query("select s.seguido.id from SeguidorEntity s where s.usuario.id = :usuarioId")
    List<Integer> findSeguidoIdsByUsuarioId(@Param("usuarioId") Integer usuarioId);
}
