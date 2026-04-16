package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chats")
@Getter
@Setter
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('MensajeDirecto','Grupo','Soporte')")
    private TipoChat tipo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    private UsuarioEntity creador;

    @Column(name = "foto_grupo_url")
    private String fotoGrupoUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ultimo_mensaje_id")
    private MensajeChatEntity ultimoMensaje;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_ultima_actividad")
    private Instant fechaUltimaActividad;

    @OneToMany(mappedBy = "chat")
    private List<IncidenciaEntity> incidencias = new ArrayList<>();

    @OneToMany(mappedBy = "chat")
    private List<MensajeChatEntity> mensajes = new ArrayList<>();

    @OneToMany(mappedBy = "chat")
    private List<UsuarioChatEntity> participantes = new ArrayList<>();
}
