package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.MensajeChatEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MensajeChatRepository extends JpaRepository<MensajeChatEntity, Integer> {

    @Query("""
            SELECT m FROM MensajeChatEntity m
            WHERE m.chat.id = :chatId
              AND (:cursor IS NULL OR m.id < :cursor)
            ORDER BY m.id DESC
            """)
    List<MensajeChatEntity> findByChatBeforeCursor(
            @Param("chatId") Integer chatId,
            @Param("cursor") Integer cursor,
            Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM MensajeChatEntity m
            JOIN m.chat c
            JOIN c.participantes uc ON uc.chat.id = :chatId AND uc.usuario.id = :userId
            WHERE m.chat.id = :chatId
              AND m.eliminadoEn IS NULL
              AND m.usuario.id != :userId
              AND (uc.fechaUltimaVista IS NULL OR m.fechaCreacion > uc.fechaUltimaVista)
            """)
    long countUnread(@Param("chatId") Integer chatId, @Param("userId") Integer userId);

    @Query("""
            SELECT m FROM MensajeChatEntity m
            WHERE m.chat.id = :chatId AND m.id = :mensajeId
            """)
    Optional<MensajeChatEntity> findByChatIdAndId(
            @Param("chatId") Integer chatId,
            @Param("mensajeId") Integer mensajeId);

    @Query("""
            SELECT m FROM MensajeChatEntity m
            JOIN FETCH m.usuario
            WHERE m.chat.id = :chatId
            ORDER BY m.id DESC
            """)
    List<MensajeChatEntity> findLatestByChatId(
            @Param("chatId") Integer chatId,
            Pageable pageable);
}
