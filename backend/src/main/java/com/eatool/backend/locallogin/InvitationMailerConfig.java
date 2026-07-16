package com.eatool.backend.locallogin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eatool.backend.emaildelivery.DefaultSmtpMailSenderFactory;
import com.eatool.backend.emaildelivery.EmailDeliveryService;
import com.eatool.backend.emaildelivery.SmtpMailSenderFactory;
import com.eatool.backend.emaildelivery.SmtpPasswordEncryptor;

/**
 * Wires the Local Login invite mailer (issue #9, ADR-0004). Since issue #25 the
 * SMTP relay is resolved dynamically at send time from the database
 * configuration (the source of truth, ADR-0010) rather than from
 * {@code spring.mail.*} at boot: {@link RelayInvitationMailer} sends via the
 * persisted relay when one exists and otherwise falls back to logging the invite
 * link ({@link LoggingInvitationMailer}) so the flow works in dev without mail
 * infrastructure (ADR-0009).
 */
@Configuration
public class InvitationMailerConfig {

    @Bean
    public SmtpMailSenderFactory smtpMailSenderFactory() {
        return new DefaultSmtpMailSenderFactory();
    }

    @Bean
    public InvitationMailer invitationMailer(
            EmailDeliveryService emailDeliveryService,
            SmtpPasswordEncryptor passwordEncryptor,
            SmtpMailSenderFactory smtpMailSenderFactory) {
        return new RelayInvitationMailer(
                emailDeliveryService,
                passwordEncryptor,
                smtpMailSenderFactory,
                new LoggingInvitationMailer());
    }
}
