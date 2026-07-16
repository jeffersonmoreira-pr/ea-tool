package com.eatool.backend.emaildelivery;

import static com.eatool.backend.support.OidcLogins.adminLogin;
import static com.eatool.backend.support.OidcLogins.editorLogin;
import static com.eatool.backend.support.OidcLogins.viewerLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Covers issue #23: the Admin-only Email Delivery (SMTP Relay) read API. Only
 * Admins may view the configuration; Viewers/Editors are denied; the response
 * reports whether a relay is configured and whether a password is saved, but
 * never exposes the password itself.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmailDeliveryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SmtpRelayConfigRepository repository;

    @Test
    void adminSeesEmptyStateWhenNoRelayConfigured() throws Exception {
        mockMvc.perform(get("/api/email-delivery").with(adminLogin("admin@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(false))
                .andExpect(jsonPath("$.passwordSaved").value(false))
                .andExpect(jsonPath("$.host").doesNotExist())
                .andExpect(jsonPath("$.port").doesNotExist());
    }

    @Test
    void adminSeesActiveStateWithoutLeakingPassword() throws Exception {
        repository.save(new SmtpRelayConfig(
                "smtp.example.com",
                587,
                SmtpEncryption.STARTTLS,
                true,
                "relay-user",
                "encrypted-secret",
                "no-reply@ea-tool.local"));

        mockMvc.perform(get("/api/email-delivery").with(adminLogin("admin@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.host").value("smtp.example.com"))
                .andExpect(jsonPath("$.port").value(587))
                .andExpect(jsonPath("$.encryption").value("STARTTLS"))
                .andExpect(jsonPath("$.authEnabled").value(true))
                .andExpect(jsonPath("$.username").value("relay-user"))
                .andExpect(jsonPath("$.fromAddress").value("no-reply@ea-tool.local"))
                .andExpect(jsonPath("$.passwordSaved").value(true))
                // The password and its ciphertext must never be serialized.
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordCiphertext").doesNotExist());
    }

    @Test
    void viewerCannotReadConfig() throws Exception {
        mockMvc.perform(get("/api/email-delivery").with(viewerLogin()))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorCannotReadConfig() throws Exception {
        mockMvc.perform(get("/api/email-delivery").with(editorLogin()))
                .andExpect(status().isForbidden());
    }
}
