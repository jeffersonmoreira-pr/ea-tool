package com.eatool.backend.emaildelivery;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatool.backend.common.BadRequestException;

/**
 * Resolves and persists the singleton SMTP relay configuration (issue #20). The
 * read path (issue #23) exposes the current configuration without the password;
 * the write path (issue #24) validates the input, encrypts the password at rest,
 * and keeps the existing password when the Admin submits a blank one. The
 * test-send path (issue #26) sends a probe email through the current relay so the
 * Admin can validate host/port/credentials. The clear path arrives in issue #27.
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
    private final SmtpMailSenderFactory mailSenderFactory;

    public EmailDeliveryService(
            SmtpRelayConfigRepository repository,
            SmtpPasswordEncryptor passwordEncryptor,
            SmtpMailSenderFactory mailSenderFactory) {
        this.repository = repository;
        this.passwordEncryptor = passwordEncryptor;
        this.mailSenderFactory = mailSenderFactory;
    }

    @Transactional(readOnly = true)
    public Optional<SmtpRelayConfig> getConfig() {
        return repository.findFirstByOrderByUpdatedAtDesc();
    }

    /**
     * Clears the persisted SMTP relay configuration (issue #27), reverting the
     * system to the no-relay state so invites fall back to the dev log (ADR-0009).
     * Idempotent: clearing when nothing is configured is a no-op.
     */
    @Transactional
    public void clearConfig() {
        repository.deleteAll();
    }

    /**
     * Sends a test email through the currently persisted relay (issue #26) so the
     * Admin can validate the connection before trusting delivery. Throws
     * {@link BadRequestException} when the recipient is invalid, when no relay is
     * configured, or when the send fails (carrying a readable reason).
     *
     * <p>Intentionally not {@code @Transactional}: the config read runs in its own
     * short transaction and the blocking SMTP send happens outside any transaction
     * so it never pins a pooled DB connection across the network round-trip.
     */
    public void sendTestEmail(String rawRecipient) {
        String recipient = rawRecipient == null ? "" : rawRecipient.trim();
        if (recipient.isEmpty() || !EMAIL_PATTERN.matcher(recipient).matches()) {
            throw new BadRequestException("Enter a valid recipient email.");
        }

        SmtpRelayConfig relay = repository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new BadRequestException(
                        "Configure and save the SMTP relay before sending a test email."));

        String plainPassword = relay.hasPassword()
                ? passwordEncryptor.decrypt(relay.getPasswordCiphertext())
                : null;
        JavaMailSender mailSender = mailSenderFactory.create(relay, plainPassword);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(relay.getFromAddress());
        message.setTo(recipient);
        message.setSubject("EA Tool SMTP relay test");
        message.setText("This is a test email from EA Tool confirming that the SMTP relay "
                + "configuration works. If you received it, delivery is set up correctly.");

        try {
            mailSender.send(message);
        } catch (MailException error) {
            throw new BadRequestException("Test email failed: " + readableCause(error));
        }
    }

    private static String readableCause(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null || message.isBlank() ? cause.getClass().getSimpleName() : message;
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
