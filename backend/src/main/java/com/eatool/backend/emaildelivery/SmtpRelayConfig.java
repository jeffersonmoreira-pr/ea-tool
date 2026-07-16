package com.eatool.backend.emaildelivery;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The corporate SMTP relay used to send Local Login invite emails (issue #20).
 * There is at most one configuration row (a singleton, resolved by
 * {@link EmailDeliveryService}); when absent, invites fall back to the dev log
 * (ADR-0009). The relay password is stored as ciphertext and is never exposed
 * by any read model (see {@link SmtpRelayConfigResponse}); encryption at rest
 * is introduced in the write path (issue #24).
 */
@Entity
@Table(name = "smtp_relay_config")
public class SmtpRelayConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SmtpEncryption encryption;

    @Column(nullable = false)
    private boolean authEnabled;

    @Column
    private String username;

    /**
     * The encrypted SMTP password. Null when authentication is disabled or a
     * password has not been provided yet. Never returned to the frontend.
     */
    @Column(name = "password_ciphertext")
    private String passwordCiphertext;

    @Column(nullable = false)
    private String fromAddress;

    @Column(nullable = false)
    private Instant updatedAt;

    protected SmtpRelayConfig() {
        // JPA
    }

    public SmtpRelayConfig(
            String host,
            int port,
            SmtpEncryption encryption,
            boolean authEnabled,
            String username,
            String passwordCiphertext,
            String fromAddress) {
        this.host = host;
        this.port = port;
        this.encryption = encryption;
        this.authEnabled = authEnabled;
        this.username = username;
        this.passwordCiphertext = passwordCiphertext;
        this.fromAddress = fromAddress;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public SmtpEncryption getEncryption() {
        return encryption;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordCiphertext() {
        return passwordCiphertext;
    }

    public boolean hasPassword() {
        return passwordCiphertext != null && !passwordCiphertext.isBlank();
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Applies an update from the write path (issue #24). The caller passes the
     * already-encrypted password ciphertext (or the current one when the Admin
     * left the field blank), so this entity never sees a clear-text password.
     */
    public void update(
            String host,
            int port,
            SmtpEncryption encryption,
            boolean authEnabled,
            String username,
            String passwordCiphertext,
            String fromAddress) {
        this.host = host;
        this.port = port;
        this.encryption = encryption;
        this.authEnabled = authEnabled;
        this.username = username;
        this.passwordCiphertext = passwordCiphertext;
        this.fromAddress = fromAddress;
        this.updatedAt = Instant.now();
    }
}
