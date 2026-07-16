package com.eatool.backend.locallogin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dev fallback that logs the invite link instead of sending an email, used when
 * no SMTP relay is configured (see {@code spring.mail.host}). This keeps the
 * Local Login flow fully exercisable without mail infrastructure (issue #9).
 */
public class LoggingInvitationMailer implements InvitationMailer {

    private static final Logger log = LoggerFactory.getLogger(LoggingInvitationMailer.class);

    @Override
    public void sendInvite(String email, String name, String inviteLink) {
        log.info("Local Login invite for {} ({}): set your password at {}", name, email, inviteLink);
    }
}
