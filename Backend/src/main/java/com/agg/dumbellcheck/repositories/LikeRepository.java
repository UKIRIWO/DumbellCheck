package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.LikeEntity;
import com.agg.dumbellcheck.entities.TipoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Integer> {

    Optional<LikeEntity> findByUsuarioIdAndTipoAndReferenciaId(Integer usuarioId, TipoLike tipo, Integer referenciaId);

    boolean existsByUsuarioIdAndTipoAndReferenciaId(Integer usuarioId, TipoLike tipo, Integer referenciaId);

    @Query("""
            SELECT l.referenciaId FROM LikeEntity l
            WHERE l.usuario.id = :usuarioId
              AND l.tipo = :tipo
              AND l.referenciaId IN :referenciaIds
            """)
    List<Integer> findLikedReferenceIds(
            @Param("usuarioId") Integer usuarioId,
            @Param("tipo") TipoLike tipo,
            @Param("referenciaIds") Collection<Integer> referenciaIds);
}
