package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<LikeEntity, Integer> {
}
