package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "usuarios_chat")
@Getter
@Setter
public class UsuarioChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('admin','miembro')")
    private RolChatUsuario rol;

    @Column(name = "fecha_union", nullable = false, updatable = false)
    private Instant fechaUnion;

    @Column(name = "fecha_ultima_vista")
    private Instant fechaUltimaVista;

    @Column(name = "notificaciones_activas", nullable = false)
    private boolean notificacionesActivas;
}
