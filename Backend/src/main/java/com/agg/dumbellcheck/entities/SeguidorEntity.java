package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "seguidores")
@Getter
@Setter
public class SeguidorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seguido_id", nullable = false)
    private UsuarioEntity seguido;

    @Column(name = "fecha_seguimiento", nullable = false, updatable = false)
    private Instant fechaSeguimiento;
}
