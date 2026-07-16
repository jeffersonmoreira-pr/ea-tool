package com.eatool.backend.locallogin;

/**
 * Sends the Local Login invite email carrying the one-time password-set link
 * (issue #9, ADR-0004). Two implementations exist: a real SMTP sender used when
 * corporate mail is configured, and a logging fallback for dev environments
 * where the invite link is simply logged (the acceptance criteria explicitly
 * allow "ou log do envio em ambiente de dev").
 */
public interface InvitationMailer {

    void sendInvite(String email, String name, String inviteLink);
}
