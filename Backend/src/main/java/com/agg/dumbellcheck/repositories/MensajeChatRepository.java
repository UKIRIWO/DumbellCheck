package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.MensajeChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeChatRepository extends JpaRepository<MensajeChatEntity, Integer> {
}
