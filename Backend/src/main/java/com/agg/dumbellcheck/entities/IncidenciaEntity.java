package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "incidencias")
@Getter
@Setter
public class IncidenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    @Column(nullable = false)
    private String asunto;

    @Column(name = "mensaje_inicial", nullable = false)
    private String mensajeInicial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('abierta','cerrada')")
    private EstadoIncidencia estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;
}
