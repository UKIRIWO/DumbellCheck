package com.agg.dumbellcheck.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agg.dumbellcheck.entities.BaneoEntity;

import java.util.List;

public interface BaneoRepository extends JpaRepository<BaneoEntity, Integer> {

    List<BaneoEntity> findByUsuario_IdOrderByFechaCreacionDesc(Integer usuarioId);
}
