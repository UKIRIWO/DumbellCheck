package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.ComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<ComentarioEntity, Integer> {
}
