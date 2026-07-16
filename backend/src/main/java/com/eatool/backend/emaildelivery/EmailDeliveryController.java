package com.eatool.backend.emaildelivery;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only API backing the Email Delivery (SMTP Relay) screen (issue #23):
 * read the current relay configuration. Access is restricted to Admins centrally
 * in SecurityConfig (hasRole("ADMIN") on /api/email-delivery/**). The password is
 * never returned (see {@link SmtpRelayConfigResponse}).
 */
@RestController
@RequestMapping("/api/email-delivery")
public class EmailDeliveryController {

    private final EmailDeliveryService emailDeliveryService;

    public EmailDeliveryController(EmailDeliveryService emailDeliveryService) {
        this.emailDeliveryService = emailDeliveryService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public SmtpRelayConfigResponse getConfig() {
        return emailDeliveryService.getConfig()
                .map(SmtpRelayConfigResponse::from)
                .orElseGet(SmtpRelayConfigResponse::empty);
    }
}
