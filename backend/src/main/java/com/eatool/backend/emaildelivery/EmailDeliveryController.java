package com.eatool.backend.emaildelivery;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only API backing the Email Delivery (SMTP Relay) screen (issues #23-#27):
 * read, save, test and clear the relay configuration. Access is restricted to
 * Admins centrally in SecurityConfig (hasRole("ADMIN") on /api/email-delivery/**).
 * The password is never returned (see {@link SmtpRelayConfigResponse}).
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

    @PutMapping
    public SmtpRelayConfigResponse save(@RequestBody SaveSmtpRelayConfigRequest request) {
        return SmtpRelayConfigResponse.from(emailDeliveryService.save(request));
    }

    @PostMapping("/test")
    public ResponseEntity<Void> sendTestEmail(@RequestBody SendTestEmailRequest request) {
        emailDeliveryService.sendTestEmail(request.recipient());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear() {
        emailDeliveryService.clearConfig();
        return ResponseEntity.noContent().build();
    }
}
