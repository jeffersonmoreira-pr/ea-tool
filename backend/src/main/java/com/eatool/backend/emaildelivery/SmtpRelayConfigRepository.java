package com.eatool.backend.emaildelivery;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SmtpRelayConfigRepository extends JpaRepository<SmtpRelayConfig, UUID> {

    /**
     * The SMTP relay configuration is a singleton; the most recently updated row
     * is the effective one. Returns empty when no relay has been configured yet.
     */
    Optional<SmtpRelayConfig> findFirstByOrderByUpdatedAtDesc();
}
