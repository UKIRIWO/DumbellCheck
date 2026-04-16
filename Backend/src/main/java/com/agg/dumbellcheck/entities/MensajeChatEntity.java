package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "mensajes_chat")
@Getter
@Setter
public class MensajeChatEntity {

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
    @Column(name = "tipo_mensaje", nullable = false, columnDefinition = "ENUM('texto','imagen','video','rutina','emoji')")
    private TipoMensajeChat tipoMensaje;

    @Column
    private String contenido;

    @Column(name = "archivo_url")
    private String archivoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id")
    private RutinaEntity rutina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mensaje_referencia_id")
    private MensajeChatEntity mensajeReferencia;

    @Column(name = "esta_editado", nullable = false)
    private boolean estaEditado;

    @Column(name = "eliminado_en")
    private Instant eliminadoEn;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_edicion")
    private Instant fechaEdicion;
}
