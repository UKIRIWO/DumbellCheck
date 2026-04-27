package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ejercicios")
@Getter
@Setter
public class EjercicioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @OneToMany(mappedBy = "ejercicio")
    private List<EjercicioRutinaEntity> rutinas = new ArrayList<>();

    @OneToMany(mappedBy = "ejercicio")
    private List<EjercicioPublicacionEntity> publicaciones = new ArrayList<>();

    @OneToMany(mappedBy = "ejercicio")
    private List<EjercicioGrupoMuscularEntity> gruposMusculares = new ArrayList<>();
}
