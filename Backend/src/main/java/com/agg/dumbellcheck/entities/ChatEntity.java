package com.agg.dumbellcheck.entities;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
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

    @Column(name = "public_id", unique = true, nullable = false, length = 21)
    private String publicId;

    @Column
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('directo','grupo','soporte')")
    private TipoChat tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
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

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = NanoIdUtils.randomNanoId();
        }
    }
}
