package com.eatool.backend.locallogin;

import org.springframework.mail.javamail.JavaMailSender;

import com.eatool.backend.emaildelivery.SmtpRelayConfig;

/**
 * Builds a {@link JavaMailSender} from the SMTP relay configuration persisted in
 * the database (issue #25). This replaces the boot-time {@code spring.mail.*}
 * wiring: the sender is constructed on demand from the current DB config so the
 * relay can be changed at runtime through the Email Delivery screen without a
 * redeploy (ADR-0010). The clear-text password is decrypted by the caller and
 * passed in; {@code null} when authentication is disabled.
 */
public interface SmtpMailSenderFactory {

    JavaMailSender create(SmtpRelayConfig config, String plainPassword);
}
