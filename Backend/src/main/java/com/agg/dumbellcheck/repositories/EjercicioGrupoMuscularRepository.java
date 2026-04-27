package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.EjercicioGrupoMuscularEntity;
import com.agg.dumbellcheck.entities.EjercicioGrupoMuscularId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EjercicioGrupoMuscularRepository extends JpaRepository<EjercicioGrupoMuscularEntity, EjercicioGrupoMuscularId> {
}
