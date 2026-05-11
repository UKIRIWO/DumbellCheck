package com.agg.dumbellcheck.entities;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
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

    @Column(name = "public_id", unique = true, nullable = false, length = 21)
    private String publicId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false)
    private String titulo;

    @Column
    private String descripcion;

    @Column(name = "multimedia_url")
    private String multimediaUrl;

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

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = NanoIdUtils.randomNanoId();
        }
    }
}
