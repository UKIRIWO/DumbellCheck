package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
public class NotificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('nuevo_seguidor','comentario_publicacion','like_publicacion','nueva_publicacion','sistema')")
    private TipoNotificacion tipo;

    @Column(name = "referencia_id")
    private Integer referenciaId;

    @Column(nullable = false)
    private boolean leida;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;
}
