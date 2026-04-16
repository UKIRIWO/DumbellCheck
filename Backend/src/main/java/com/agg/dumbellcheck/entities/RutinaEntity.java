package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutinas")
@Getter
@Setter
public class RutinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @Column(name = "es_publica", nullable = false)
    private boolean esPublica;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @OneToMany(mappedBy = "rutina")
    private List<EjercicioRutinaEntity> ejercicios = new ArrayList<>();

    @OneToMany(mappedBy = "rutinaOrigen")
    private List<PublicacionEntity> publicacionesOrigen = new ArrayList<>();

    @OneToMany(mappedBy = "rutina")
    private List<MensajeChatEntity> mensajesCompartidos = new ArrayList<>();
}
