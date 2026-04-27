package com.agg.dumbellcheck.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class EjercicioGrupoMuscularId implements Serializable {

    @Column(name = "ejercicio_id")
    private Integer ejercicioId;

    @Column(name = "grupo_muscular_id")
    private Integer grupoMuscularId;
}
