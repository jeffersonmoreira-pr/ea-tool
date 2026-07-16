package com.eatool.backend.locallogin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Selects the {@link InvitationMailer} implementation: the SMTP-backed sender
 * when a corporate relay is configured ({@code spring.mail.host}), or the
 * logging fallback otherwise so the Local Login flow works in dev without mail
 * infrastructure (issue #9, ADR-0004).
 */
@Configuration
public class InvitationMailerConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.mail", name = "host")
    public InvitationMailer smtpInvitationMailer(
            JavaMailSender mailSender,
            @Value("${app.local-login.mail-from:no-reply@ea-tool.local}") String fromAddress) {
        return new SmtpInvitationMailer(mailSender, fromAddress);
    }

    @Bean
    @ConditionalOnMissingBean(InvitationMailer.class)
    public InvitationMailer loggingInvitationMailer() {
        return new LoggingInvitationMailer();
    }
}
