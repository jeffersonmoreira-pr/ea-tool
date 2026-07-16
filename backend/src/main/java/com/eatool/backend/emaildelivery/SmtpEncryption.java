package com.eatool.backend.emaildelivery;

/**
 * Transport security used when connecting to the SMTP relay (issue #23). Mirrors
 * the segmented control in the Email Delivery screen (see design/Email_Delivery.md):
 * no encryption, opportunistic STARTTLS, or implicit SSL/TLS.
 */
public enum SmtpEncryption {
    NONE,
    STARTTLS,
    SSL_TLS
}
