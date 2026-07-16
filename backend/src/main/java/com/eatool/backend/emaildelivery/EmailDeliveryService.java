package com.eatool.backend.emaildelivery;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the singleton SMTP relay configuration (issue #20). The read path
 * (issue #23) exposes the current configuration without the password; the write,
 * test-send and clear paths arrive in later slices (issues #24–#27).
 */
@Service
public class EmailDeliveryService {

    private final SmtpRelayConfigRepository repository;

    public EmailDeliveryService(SmtpRelayConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<SmtpRelayConfig> getConfig() {
        return repository.findFirstByOrderByUpdatedAtDesc();
    }
}
