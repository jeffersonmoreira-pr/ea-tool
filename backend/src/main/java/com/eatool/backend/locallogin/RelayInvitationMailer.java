package com.eatool.backend.locallogin;

import java.util.Optional;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.eatool.backend.emaildelivery.EmailDeliveryService;
import com.eatool.backend.emaildelivery.SmtpPasswordEncryptor;
import com.eatool.backend.emaildelivery.SmtpRelayConfig;

/**
 * Runtime-dynamic {@link InvitationMailer} (issue #25): on every send it reads
 * the current SMTP relay configuration from the database (the source of truth,
 * per ADR-0010). When a relay is configured, it decrypts the stored password,
 * builds a {@link JavaMailSender} from that config and sends the invite via
 * SMTP; when no relay is configured, it delegates to the logging fallback so the
 * Local Login flow stays usable in dev without mail infrastructure (ADR-0009).
 *
 * <p>The selection happens per send (not fixed at boot), so saving or clearing
 * the relay through the Email Delivery screen takes effect immediately, and the
 * DB config takes precedence over any {@code spring.mail.*} settings.
 */
public class RelayInvitationMailer implements InvitationMailer {

    private final EmailDeliveryService emailDeliveryService;
    private final SmtpPasswordEncryptor passwordEncryptor;
    private final SmtpMailSenderFactory mailSenderFactory;
    private final InvitationMailer fallbackMailer;

    public RelayInvitationMailer(
            EmailDeliveryService emailDeliveryService,
            SmtpPasswordEncryptor passwordEncryptor,
            SmtpMailSenderFactory mailSenderFactory,
            InvitationMailer fallbackMailer) {
        this.emailDeliveryService = emailDeliveryService;
        this.passwordEncryptor = passwordEncryptor;
        this.mailSenderFactory = mailSenderFactory;
        this.fallbackMailer = fallbackMailer;
    }

    @Override
    public void sendInvite(String email, String name, String inviteLink) {
        Optional<SmtpRelayConfig> config = emailDeliveryService.getConfig();
        if (config.isEmpty()) {
            fallbackMailer.sendInvite(email, name, inviteLink);
            return;
        }

        SmtpRelayConfig relay = config.get();
        String plainPassword = relay.hasPassword()
                ? passwordEncryptor.decrypt(relay.getPasswordCiphertext())
                : null;
        JavaMailSender mailSender = mailSenderFactory.create(relay, plainPassword);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(relay.getFromAddress());
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
