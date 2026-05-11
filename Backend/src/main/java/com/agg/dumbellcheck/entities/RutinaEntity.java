package com.agg.dumbellcheck.entities;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
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

    @Column(name = "public_id", unique = true, nullable = false, length = 21)
    private String publicId;

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

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = NanoIdUtils.randomNanoId();
        }
    }

    @OneToMany(mappedBy = "rutina")
    private List<EjercicioRutinaEntity> ejercicios = new ArrayList<>();

    @OneToMany(mappedBy = "rutina")
    private List<MensajeChatEntity> mensajesCompartidos = new ArrayList<>();
}
