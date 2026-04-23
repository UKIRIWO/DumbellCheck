package com.agg.dumbellcheck.repositories;

import com.agg.dumbellcheck.entities.AuthIdentityEntity;
import com.agg.dumbellcheck.entities.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentityEntity, Integer> {
    Optional<AuthIdentityEntity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
