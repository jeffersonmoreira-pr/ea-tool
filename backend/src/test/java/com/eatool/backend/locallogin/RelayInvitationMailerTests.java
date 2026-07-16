package com.eatool.backend.locallogin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.eatool.backend.emaildelivery.EmailDeliveryService;
import com.eatool.backend.emaildelivery.SmtpEncryption;
import com.eatool.backend.emaildelivery.SmtpPasswordEncryptor;
import com.eatool.backend.emaildelivery.SmtpRelayConfig;

/**
 * Unit test for {@link RelayInvitationMailer} (issue #25): the invite is sent via
 * the SMTP relay persisted in the database (with the password decrypted at send
 * time) when one is configured, and falls back to logging when none is.
 */
class RelayInvitationMailerTests {

    private static final String KEY =
            Base64.getEncoder().encodeToString("relay-mailer-aes-key-32-bytes!!!".getBytes());

    @Test
    void sendsViaRelayFromDatabaseUsingDecryptedPassword() {
        SmtpPasswordEncryptor encryptor = new SmtpPasswordEncryptor(KEY);
        String ciphertext = encryptor.encrypt("relay-secret");

        SmtpRelayConfig relay = new SmtpRelayConfig(
                "smtp.example.com", 587, SmtpEncryption.STARTTLS,
                true, "relay-user", ciphertext, "no-reply@ea-tool.local");

        EmailDeliveryService emailDeliveryService = mock(EmailDeliveryService.class);
        when(emailDeliveryService.getConfig()).thenReturn(Optional.of(relay));

        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpMailSenderFactory factory = mock(SmtpMailSenderFactory.class);
        when(factory.create(relay, "relay-secret")).thenReturn(mailSender);

        InvitationMailer fallback = mock(InvitationMailer.class);

        RelayInvitationMailer mailer = new RelayInvitationMailer(
                emailDeliveryService, encryptor, factory, fallback);

        mailer.sendInvite("new.user@example.com", "New User", "https://ea-tool/set-password?token=abc");

        // The decrypted password must be handed to the factory.
        verify(factory).create(relay, "relay-secret");
        verifyNoInteractions(fallback);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getFrom()).isEqualTo("no-reply@ea-tool.local");
        assertThat(sent.getTo()).containsExactly("new.user@example.com");
        assertThat(sent.getText()).contains("https://ea-tool/set-password?token=abc");
    }

    @Test
    void sendsViaRelayWithoutPasswordWhenAuthDisabled() {
        SmtpPasswordEncryptor encryptor = new SmtpPasswordEncryptor(KEY);
        SmtpRelayConfig relay = new SmtpRelayConfig(
                "smtp.internal", 25, SmtpEncryption.NONE,
                false, null, null, "no-reply@ea-tool.local");

        EmailDeliveryService emailDeliveryService = mock(EmailDeliveryService.class);
        when(emailDeliveryService.getConfig()).thenReturn(Optional.of(relay));

        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpMailSenderFactory factory = mock(SmtpMailSenderFactory.class);
        when(factory.create(relay, null)).thenReturn(mailSender);

        InvitationMailer fallback = mock(InvitationMailer.class);

        RelayInvitationMailer mailer = new RelayInvitationMailer(
                emailDeliveryService, encryptor, factory, fallback);

        mailer.sendInvite("user@example.com", "User", "https://ea-tool/set-password?token=xyz");

        verify(factory).create(relay, null);
        verify(mailSender).send(any(SimpleMailMessage.class));
        verifyNoInteractions(fallback);
    }

    @Test
    void fallsBackToLoggingWhenNoRelayConfigured() {
        EmailDeliveryService emailDeliveryService = mock(EmailDeliveryService.class);
        when(emailDeliveryService.getConfig()).thenReturn(Optional.empty());

        SmtpMailSenderFactory factory = mock(SmtpMailSenderFactory.class);
        InvitationMailer fallback = mock(InvitationMailer.class);
        SmtpPasswordEncryptor encryptor = new SmtpPasswordEncryptor(KEY);

        RelayInvitationMailer mailer = new RelayInvitationMailer(
                emailDeliveryService, encryptor, factory, fallback);

        mailer.sendInvite("user@example.com", "User", "https://ea-tool/set-password?token=xyz");

        verify(fallback).sendInvite("user@example.com", "User", "https://ea-tool/set-password?token=xyz");
        verifyNoInteractions(factory);
    }

    @Test
    void neverConsultsFactoryOrSenderInFallback() {
        EmailDeliveryService emailDeliveryService = mock(EmailDeliveryService.class);
        when(emailDeliveryService.getConfig()).thenReturn(Optional.empty());

        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpMailSenderFactory factory = mock(SmtpMailSenderFactory.class);
        InvitationMailer fallback = mock(InvitationMailer.class);
        SmtpPasswordEncryptor encryptor = new SmtpPasswordEncryptor(KEY);

        RelayInvitationMailer mailer = new RelayInvitationMailer(
                emailDeliveryService, encryptor, factory, fallback);

        mailer.sendInvite("user@example.com", "User", "link");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
