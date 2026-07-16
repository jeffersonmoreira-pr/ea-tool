package com.eatool.backend.locallogin;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Sends the Local Login invite via the corporate SMTP relay (issue #9). Wired
 * only when {@code spring.mail.host} is configured; otherwise the logging
 * fallback is used (see {@link InvitationMailerConfig}).
 */
public class SmtpInvitationMailer implements InvitationMailer {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpInvitationMailer(JavaMailSender mailSender, String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendInvite(String email, String name, String inviteLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Set your EA Tool password");
        message.setText("Hi " + name + ",\n\n"
                + "An account was created for you on EA Tool. "
                + "Set your password using the link below (it can be used only once and will expire):\n\n"
                + inviteLink + "\n\n"
                + "If you did not expect this, you can ignore this email.");
        mailSender.send(message);
    }
}
