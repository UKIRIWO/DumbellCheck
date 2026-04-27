package com.agg.dumbellcheck.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ejercicios_grupos_musculares")
@Getter
@Setter
public class EjercicioGrupoMuscularEntity {

    @EmbeddedId
    private EjercicioGrupoMuscularId id = new EjercicioGrupoMuscularId();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("ejercicioId")
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private EjercicioEntity ejercicio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("grupoMuscularId")
    @JoinColumn(name = "grupo_muscular_id", nullable = false)
    private GrupoMuscularEntity grupoMuscular;
}
