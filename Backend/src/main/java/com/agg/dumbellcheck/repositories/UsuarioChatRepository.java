package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.UsuarioChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioChatRepository extends JpaRepository<UsuarioChatEntity, Integer> {
}
