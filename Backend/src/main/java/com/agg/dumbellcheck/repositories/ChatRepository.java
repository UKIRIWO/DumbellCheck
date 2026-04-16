package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatEntity, Integer> {
}
