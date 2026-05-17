package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.RolChatUsuario;
import com.agg.dumbellcheck.entities.UsuarioChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UsuarioChatRepository extends JpaRepository<UsuarioChatEntity, Integer> {

    List<UsuarioChatEntity> findByChatId(Integer chatId);

    List<UsuarioChatEntity> findByChatIdOrderByIdAsc(Integer chatId);

    Optional<UsuarioChatEntity> findByChat_IdAndUsuario_Id(Integer chatId, Integer usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM UsuarioChatEntity uc
            WHERE uc.chat.id = :chatId AND uc.usuario.id = :userId
            """)
    int deleteMembership(@Param("chatId") Integer chatId, @Param("userId") Integer userId);

    @Modifying
    @Query("""
            UPDATE UsuarioChatEntity uc
            SET uc.fechaUltimaVista = :now
            WHERE uc.chat.id = :chatId AND uc.usuario.id = :userId
            """)
    void updateLastSeen(@Param("chatId") Integer chatId,
                        @Param("userId") Integer userId,
                        @Param("now") Instant now);
}
