package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "baneos")
@Getter
@Setter
public class BaneoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "baneado_hasta")
    private Instant baneadoHasta;

    @Column(name = "baneado_permanentemente", nullable = false)
    private boolean baneadoPermanentemente;

    @Column(name = "motivo_baneo")
    private String motivoBaneo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;
}
