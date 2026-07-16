package com.eatool.backend.locallogin;

import java.util.Properties;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.eatool.backend.emaildelivery.SmtpEncryption;
import com.eatool.backend.emaildelivery.SmtpRelayConfig;

/**
 * Default {@link SmtpMailSenderFactory}: builds a {@link JavaMailSenderImpl}
 * from the persisted relay configuration (issue #25), translating the
 * {@link SmtpEncryption} mode into the corresponding JavaMail properties
 * (STARTTLS vs. implicit SSL/TLS) and applying credentials only when
 * authentication is enabled.
 */
public class DefaultSmtpMailSenderFactory implements SmtpMailSenderFactory {

    @Override
    public JavaMailSender create(SmtpRelayConfig config, String plainPassword) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getHost());
        sender.setPort(config.getPort());

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        // Finite timeouts so a slow or unreachable relay cannot hang the invite
        // send indefinitely (the send runs inside the user-creation transaction,
        // so an unbounded wait would pin the DB connection). Values in ms.
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        if (config.isAuthEnabled()) {
            sender.setUsername(config.getUsername());
            sender.setPassword(plainPassword);
            properties.put("mail.smtp.auth", "true");
        } else {
            properties.put("mail.smtp.auth", "false");
        }

        SmtpEncryption encryption = config.getEncryption();
        if (encryption == SmtpEncryption.STARTTLS) {
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.starttls.required", "true");
        } else if (encryption == SmtpEncryption.SSL_TLS) {
            properties.put("mail.smtp.ssl.enable", "true");
        }

        return sender;
    }
}
