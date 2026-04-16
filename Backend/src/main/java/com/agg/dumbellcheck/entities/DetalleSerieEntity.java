package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "detalles_series")
@Getter
@Setter
public class DetalleSerieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ejercicio_publicacion_id", nullable = false)
    private EjercicioPublicacionEntity ejercicioPublicacion;

    @Column(name = "numero_serie", nullable = false)
    private Integer numeroSerie;

    @Column(nullable = false)
    private Integer repeticiones;

    @Column
    private BigDecimal peso;

    @Column(name = "descanso_segundos")
    private Integer descansoSegundos;

    @Column
    private Integer rpe;

    @Column
    private String notas;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;
}
