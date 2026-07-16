package com.eatool.backend.emaildelivery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;


/**
 * Unit test for {@link DefaultSmtpMailSenderFactory} (issue #25): the persisted
 * relay config is translated into a JavaMailSender with the right host/port,
 * credentials (only when auth is enabled), and JavaMail encryption properties.
 */
class DefaultSmtpMailSenderFactoryTests {

    private final DefaultSmtpMailSenderFactory factory = new DefaultSmtpMailSenderFactory();

    @Test
    void mapsStartTlsWithAuthentication() {
        SmtpRelayConfig config = new SmtpRelayConfig(
                "smtp.example.com", 587, SmtpEncryption.STARTTLS,
                true, "relay-user", "ignored-ciphertext", "no-reply@ea-tool.local");

        JavaMailSenderImpl sender = (JavaMailSenderImpl) factory.create(config, "plain-pass");

        assertThat(sender.getHost()).isEqualTo("smtp.example.com");
        assertThat(sender.getPort()).isEqualTo(587);
        assertThat(sender.getUsername()).isEqualTo("relay-user");
        assertThat(sender.getPassword()).isEqualTo("plain-pass");
        assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.auth")).isEqualTo("true");
        assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
    }

    @Test
    void mapsImplicitSslTls() {
        SmtpRelayConfig config = new SmtpRelayConfig(
                "smtp.example.com", 465, SmtpEncryption.SSL_TLS,
                true, "relay-user", "ignored-ciphertext", "no-reply@ea-tool.local");

        JavaMailSenderImpl sender = (JavaMailSenderImpl) factory.create(config, "plain-pass");

        assertThat(sender.getPort()).isEqualTo(465);
        assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.ssl.enable")).isEqualTo("true");
        assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.starttls.enable")).isNull();
    }

    @Test
    void omitsCredentialsWhenAuthDisabled() {
        SmtpRelayConfig config = new SmtpRelayConfig(
                "smtp.internal", 25, SmtpEncryption.NONE,
                false, null, null, "no-reply@ea-tool.local");

        JavaMailSenderImpl sender = (JavaMailSenderImpl) factory.create(config, null);

        assertThat(sender.getUsername()).isNull();
        assertThat(sender.getPassword()).isNull();
        assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.auth")).isEqualTo("false");
        assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.ssl.enable")).isNull();
        assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.starttls.enable")).isNull();
    }
}
