package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.PublicacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicacionRepository extends JpaRepository<PublicacionEntity, Integer> {

    Page<PublicacionEntity> findByEstaActivaTrueOrderByFechaCreacionDesc(Pageable pageable);

    java.util.Optional<PublicacionEntity> findByIdAndEstaActivaTrue(Integer id);
}
