package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.ChatEntity;
import com.agg.dumbellcheck.entities.TipoChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatEntity, Integer> {

    Optional<ChatEntity> findByPublicId(String publicId);

    @Query("""
            SELECT c FROM ChatEntity c
            JOIN c.participantes uc
            WHERE uc.usuario.id = :userId
              AND c.tipo != com.agg.dumbellcheck.entities.TipoChat.soporte
            ORDER BY c.fechaUltimaActividad DESC
            """)
    List<ChatEntity> findChatsForUser(@Param("userId") Integer userId);

    @Query("""
            SELECT c FROM ChatEntity c
            JOIN c.participantes p1
            JOIN c.participantes p2
            WHERE c.tipo = com.agg.dumbellcheck.entities.TipoChat.directo
              AND p1.usuario.id = :userId1
              AND p2.usuario.id = :userId2
            """)
    Optional<ChatEntity> findDirectChatBetween(
            @Param("userId1") Integer userId1,
            @Param("userId2") Integer userId2);

    @Query("""
            SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END
            FROM UsuarioChatEntity uc
            WHERE uc.chat.id = :chatId AND uc.usuario.id = :userId
            """)
    boolean isMember(@Param("chatId") Integer chatId, @Param("userId") Integer userId);
}
