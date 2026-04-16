package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.EjercicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EjercicioRepository extends JpaRepository<EjercicioEntity, Integer> {
}
