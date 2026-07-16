package com.eatool.backend.locallogin;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A single-use, expiring invitation that lets a freshly created Local Login
 * user set their password once (issue #9, ADR-0004). The token travels in the
 * invite email link; it is consumed on first successful password set, and
 * rejected afterwards or once expired.
 */
@Entity
@Table(name = "local_login_invitations")
public class LocalLoginInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID catalogUserId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected LocalLoginInvitation() {
        // JPA
    }

    public LocalLoginInvitation(UUID catalogUserId, String token, Instant expiresAt) {
        this.catalogUserId = catalogUserId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.used = false;
        this.createdAt = Instant.now();
    }

    /** True when the invitation may still be used to set a password. */
    public boolean isRedeemable(Instant now) {
        return !used && now.isBefore(expiresAt);
    }

    public void markUsed() {
        this.used = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCatalogUserId() {
        return catalogUserId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
