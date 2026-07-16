package com.eatool.backend.emaildelivery;

/**
 * Write model for the SMTP relay test-send (issue #26): the recipient the Admin
 * wants the probe email delivered to. The current persisted relay configuration
 * is used for host/port/credentials.
 */
public record SendTestEmailRequest(String recipient) {
}
