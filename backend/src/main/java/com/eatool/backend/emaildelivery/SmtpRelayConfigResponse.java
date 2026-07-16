package com.eatool.backend.emaildelivery;

/**
 * Read model for the Email Delivery screen (issue #23). Reports whether a relay
 * is configured and its non-secret settings. The SMTP password is never
 * included; {@code passwordSaved} only tells the UI whether a password exists so
 * it can render the "•••••••• (saved)" placeholder (see design/Email_Delivery.md).
 */
public record SmtpRelayConfigResponse(
        boolean configured,
        String host,
        Integer port,
        SmtpEncryption encryption,
        boolean authEnabled,
        String username,
        String fromAddress,
        boolean passwordSaved) {

    public static SmtpRelayConfigResponse empty() {
        return new SmtpRelayConfigResponse(false, null, null, null, false, null, null, false);
    }

    public static SmtpRelayConfigResponse from(SmtpRelayConfig config) {
        return new SmtpRelayConfigResponse(
                true,
                config.getHost(),
                config.getPort(),
                config.getEncryption(),
                config.isAuthEnabled(),
                config.getUsername(),
                config.getFromAddress(),
                config.hasPassword());
    }
}
