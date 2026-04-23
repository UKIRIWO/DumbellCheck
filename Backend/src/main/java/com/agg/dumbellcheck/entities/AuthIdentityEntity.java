package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "auth_identities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_auth_provider_user", columnNames = {"provider", "provider_user_id"}),
                @UniqueConstraint(name = "uk_auth_user_provider", columnNames = {"usuario_id", "provider"})
        }
)
@Getter
@Setter
public class AuthIdentityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 191)
    private String providerUserId;

    @Column(name = "email_provider", nullable = false)
    private String emailProvider;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
