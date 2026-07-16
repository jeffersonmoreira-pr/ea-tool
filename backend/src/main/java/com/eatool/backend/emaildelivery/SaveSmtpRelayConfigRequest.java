package com.eatool.backend.emaildelivery;

/**
 * Write model for saving the SMTP relay configuration (issue #24). {@code port}
 * and {@code encryption} are boxed/String so the service can report a precise
 * validation message when they are missing or invalid rather than failing to
 * deserialize. {@code password} is optional: a blank value keeps the currently
 * stored password (see {@link EmailDeliveryService#save}).
 */
public record SaveSmtpRelayConfigRequest(
        String host,
        Integer port,
        String encryption,
        boolean authEnabled,
        String username,
        String password,
        String fromAddress) {
}
