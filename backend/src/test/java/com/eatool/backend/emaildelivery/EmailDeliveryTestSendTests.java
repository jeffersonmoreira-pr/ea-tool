package com.eatool.backend.emaildelivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.eatool.backend.common.BadRequestException;

/**
 * Unit test for the SMTP relay test-send path (issue #26): a probe email is sent
 * through the current relay using the decrypted password; invalid recipients,
 * missing configuration, and send failures are reported as readable errors.
 */
class EmailDeliveryTestSendTests {

    private static final String KEY =
            Base64.getEncoder().encodeToString("test-send-aes-key-32-bytes-long!".getBytes());

    private final SmtpPasswordEncryptor encryptor = new SmtpPasswordEncryptor(KEY);

    private SmtpRelayConfig relay(String ciphertext) {
        return new SmtpRelayConfig(
                "smtp.example.com", 587, SmtpEncryption.STARTTLS,
                true, "relay-user", ciphertext, "no-reply@ea-tool.local");
    }

    @Test
    void sendsTestEmailThroughCurrentRelayWithDecryptedPassword() {
        String ciphertext = encryptor.encrypt("relay-secret");
        SmtpRelayConfig config = relay(ciphertext);

        SmtpRelayConfigRepository repository = mock(SmtpRelayConfigRepository.class);
        when(repository.findFirstByOrderByUpdatedAtDesc()).thenReturn(Optional.of(config));

        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpMailSenderFactory factory = mock(SmtpMailSenderFactory.class);
        when(factory.create(config, "relay-secret")).thenReturn(mailSender);

        EmailDeliveryService service = new EmailDeliveryService(repository, encryptor, factory);

        service.sendTestEmail("recipient@example.com");

        verify(factory).create(config, "relay-secret");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void reportsReadableReasonWhenSendFails() {
        SmtpRelayConfig config = relay(encryptor.encrypt("relay-secret"));

        SmtpRelayConfigRepository repository = mock(SmtpRelayConfigRepository.class);
        when(repository.findFirstByOrderByUpdatedAtDesc()).thenReturn(Optional.of(config));

        JavaMailSender mailSender = mock(JavaMailSender.class);
        doThrow(new MailSendException("Connection refused: check host and port"))
                .when(mailSender).send(any(SimpleMailMessage.class));
        SmtpMailSenderFactory factory = mock(SmtpMailSenderFactory.class);
        when(factory.create(any(), any())).thenReturn(mailSender);

        EmailDeliveryService service = new EmailDeliveryService(repository, encryptor, factory);

        assertThatThrownBy(() -> service.sendTestEmail("recipient@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Test email failed")
                .hasMessageContaining("Connection refused");
    }

    @Test
    void rejectsInvalidRecipientWithoutSending() {
        SmtpRelayConfigRepository repository = mock(SmtpRelayConfigRepository.class);
        SmtpMailSenderFactory factory = mock(SmtpMailSenderFactory.class);
        EmailDeliveryService service = new EmailDeliveryService(repository, encryptor, factory);

        assertThatThrownBy(() -> service.sendTestEmail("not-an-email"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("valid recipient email");

        verifyNoInteractions(factory);
    }

    @Test
    void failsWhenNoRelayConfigured() {
        SmtpRelayConfigRepository repository = mock(SmtpRelayConfigRepository.class);
        when(repository.findFirstByOrderByUpdatedAtDesc()).thenReturn(Optional.empty());
        SmtpMailSenderFactory factory = mock(SmtpMailSenderFactory.class);
        EmailDeliveryService service = new EmailDeliveryService(repository, encryptor, factory);

        assertThatThrownBy(() -> service.sendTestEmail("recipient@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Configure and save the SMTP relay");

        verifyNoInteractions(factory);
    }

    @Test
    void sendsWithoutPasswordWhenAuthDisabled() {
        SmtpRelayConfig config = new SmtpRelayConfig(
                "smtp.internal", 25, SmtpEncryption.NONE,
                false, null, null, "no-reply@ea-tool.local");

        SmtpRelayConfigRepository repository = mock(SmtpRelayConfigRepository.class);
        when(repository.findFirstByOrderByUpdatedAtDesc()).thenReturn(Optional.of(config));

        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpMailSenderFactory factory = mock(SmtpMailSenderFactory.class);
        when(factory.create(config, null)).thenReturn(mailSender);

        EmailDeliveryService service = new EmailDeliveryService(repository, encryptor, factory);

        service.sendTestEmail("recipient@example.com");

        verify(factory).create(config, null);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
