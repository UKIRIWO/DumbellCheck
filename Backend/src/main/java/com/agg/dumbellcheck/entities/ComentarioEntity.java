package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comentarios")
@Getter
@Setter
public class ComentarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "publicacion_id", nullable = false)
    private PublicacionEntity publicacion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comentario_padre_id")
    private ComentarioEntity comentarioPadre;

    @Column(nullable = false)
    private String texto;

    @Column(name = "contador_likes", nullable = false)
    private Integer contadorLikes;

    @Column(name = "esta_activo", nullable = false)
    private boolean estaActivo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @OneToMany(mappedBy = "comentarioPadre")
    private List<ComentarioEntity> respuestas = new ArrayList<>();
}
