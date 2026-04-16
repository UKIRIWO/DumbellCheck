package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publicaciones")
@Getter
@Setter
public class PublicacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_origen_id")
    private RutinaEntity rutinaOrigen;

    @Column(nullable = false)
    private String titulo;

    @Column
    private String descripcion;

    @Column(name = "multimedia_url")
    private String multimediaUrl;

    @Column(name = "fecha_entreno")
    private LocalDate fechaEntreno;

    @Column(name = "contador_likes", nullable = false)
    private Integer contadorLikes;

    @Column(name = "contador_comentarios", nullable = false)
    private Integer contadorComentarios;

    @Column(name = "esta_activa", nullable = false)
    private boolean estaActiva;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @OneToMany(mappedBy = "publicacion")
    private List<EjercicioPublicacionEntity> ejercicios = new ArrayList<>();

    @OneToMany(mappedBy = "publicacion")
    private List<ComentarioEntity> comentarios = new ArrayList<>();
}
