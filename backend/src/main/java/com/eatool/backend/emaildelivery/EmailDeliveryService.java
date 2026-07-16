package com.eatool.backend.emaildelivery;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatool.backend.common.BadRequestException;

/**
 * Resolves and persists the singleton SMTP relay configuration (issue #20). The
 * read path (issue #23) exposes the current configuration without the password;
 * the write path (issue #24) validates the input, encrypts the password at rest,
 * and keeps the existing password when the Admin submits a blank one. Test-send
 * and clear paths arrive in later slices (issues #26-#27).
 */
@Service
public class EmailDeliveryService {

    private static final Pattern HOST_PATTERN =
            Pattern.compile("^(?=.{1,253}$)([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)"
                    + "(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final SmtpRelayConfigRepository repository;
    private final SmtpPasswordEncryptor passwordEncryptor;

    public EmailDeliveryService(
            SmtpRelayConfigRepository repository, SmtpPasswordEncryptor passwordEncryptor) {
        this.repository = repository;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Transactional(readOnly = true)
    public Optional<SmtpRelayConfig> getConfig() {
        return repository.findFirstByOrderByUpdatedAtDesc();
    }

    @Transactional
    public SmtpRelayConfig save(SaveSmtpRelayConfigRequest request) {
        String host = requireText(request.host(), "Host is required.");
        if (!HOST_PATTERN.matcher(host).matches()) {
            throw new BadRequestException("Enter a valid hostname.");
        }

        Integer port = request.port();
        if (port == null || port < 1 || port > 65535) {
            throw new BadRequestException("Port must be between 1 and 65535.");
        }

        SmtpEncryption encryption = parseEncryption(request.encryption());

        String fromAddress = requireText(request.fromAddress(), "From address is required.");
        if (!EMAIL_PATTERN.matcher(fromAddress).matches()) {
            throw new BadRequestException("Enter a valid from address.");
        }

        Optional<SmtpRelayConfig> existing = repository.findFirstByOrderByUpdatedAtDesc();

        boolean authEnabled = request.authEnabled();
        String username = null;
        String passwordCiphertext = null;
        if (authEnabled) {
            username = requireText(request.username(), "Username is required when authentication is enabled.");

            String submittedPassword = request.password();
            if (submittedPassword != null && !submittedPassword.isBlank()) {
                passwordCiphertext = passwordEncryptor.encrypt(submittedPassword);
            } else {
                passwordCiphertext = existing
                        .filter(SmtpRelayConfig::hasPassword)
                        .map(SmtpRelayConfig::getPasswordCiphertext)
                        .orElseThrow(() -> new BadRequestException(
                                "Password is required when authentication is enabled."));
            }
        }

        SmtpRelayConfig config = existing.orElse(null);
        if (config == null) {
            config = new SmtpRelayConfig(
                    host, port, encryption, authEnabled, username, passwordCiphertext, fromAddress);
        } else {
            config.update(host, port, encryption, authEnabled, username, passwordCiphertext, fromAddress);
        }
        return repository.save(config);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private static SmtpEncryption parseEncryption(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Encryption is required.");
        }
        try {
            return SmtpEncryption.valueOf(value.trim());
        } catch (IllegalArgumentException error) {
            throw new BadRequestException("Encryption must be one of NONE, STARTTLS or SSL_TLS.");
        }
    }
}
