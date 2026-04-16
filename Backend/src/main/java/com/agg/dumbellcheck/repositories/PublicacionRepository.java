package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.PublicacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicacionRepository extends JpaRepository<PublicacionEntity, Integer> {
}
