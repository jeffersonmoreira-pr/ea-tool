package com.eatool.backend.emaildelivery;

import static com.eatool.backend.support.OidcLogins.adminLogin;
import static com.eatool.backend.support.OidcLogins.editorLogin;
import static com.eatool.backend.support.OidcLogins.viewerLogin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Covers issues #23 (read) and #24 (save) of the Admin-only Email Delivery
 * (SMTP Relay) API. Only Admins may view or save the configuration;
 * Viewers/Editors are denied. The response reports whether a relay is configured
 * and whether a password is saved, but never exposes the password. On save, the
 * password is encrypted at rest, a blank password keeps the current one, and the
 * host/port/from/username inputs are validated.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmailDeliveryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SmtpRelayConfigRepository repository;

    @Autowired
    private SmtpPasswordEncryptor passwordEncryptor;

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

    @Test
    void adminSavesConfigEncryptingPasswordAtRestWithoutLeaking() throws Exception {
        String body = """
                {
                  "host": "smtp.example.com",
                  "port": 587,
                  "encryption": "STARTTLS",
                  "authEnabled": true,
                  "username": "relay-user",
                  "password": "s3cr3t-pass",
                  "fromAddress": "no-reply@ea-tool.local"
                }
                """;

        mockMvc.perform(put("/api/email-delivery")
                        .with(adminLogin("admin@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.host").value("smtp.example.com"))
                .andExpect(jsonPath("$.passwordSaved").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordCiphertext").doesNotExist());

        SmtpRelayConfig saved = repository.findFirstByOrderByUpdatedAtDesc().orElseThrow();
        assertThat(saved.getPasswordCiphertext()).isNotEqualTo("s3cr3t-pass");
        assertThat(passwordEncryptor.decrypt(saved.getPasswordCiphertext())).isEqualTo("s3cr3t-pass");
    }

    @Test
    void blankPasswordKeepsTheCurrentPassword() throws Exception {
        String first = """
                {
                  "host": "smtp.example.com",
                  "port": 587,
                  "encryption": "STARTTLS",
                  "authEnabled": true,
                  "username": "relay-user",
                  "password": "original-pass",
                  "fromAddress": "no-reply@ea-tool.local"
                }
                """;
        mockMvc.perform(put("/api/email-delivery")
                        .with(adminLogin("admin@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isOk());
        String storedCiphertext = repository.findFirstByOrderByUpdatedAtDesc().orElseThrow().getPasswordCiphertext();

        String second = """
                {
                  "host": "smtp2.example.com",
                  "port": 2525,
                  "encryption": "SSL_TLS",
                  "authEnabled": true,
                  "username": "relay-user",
                  "password": "",
                  "fromAddress": "no-reply@ea-tool.local"
                }
                """;
        mockMvc.perform(put("/api/email-delivery")
                        .with(adminLogin("admin@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.host").value("smtp2.example.com"))
                .andExpect(jsonPath("$.passwordSaved").value(true));

        SmtpRelayConfig updated = repository.findFirstByOrderByUpdatedAtDesc().orElseThrow();
        assertThat(updated.getPasswordCiphertext()).isEqualTo(storedCiphertext);
        assertThat(passwordEncryptor.decrypt(updated.getPasswordCiphertext())).isEqualTo("original-pass");
    }

    @Test
    void rejectsInvalidPort() throws Exception {
        String body = """
                {
                  "host": "smtp.example.com",
                  "port": 70000,
                  "encryption": "STARTTLS",
                  "authEnabled": false,
                  "fromAddress": "no-reply@ea-tool.local"
                }
                """;
        mockMvc.perform(put("/api/email-delivery")
                        .with(adminLogin("admin@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Port must be between 1 and 65535."));
    }

    @Test
    void rejectsInvalidFromAddress() throws Exception {
        String body = """
                {
                  "host": "smtp.example.com",
                  "port": 587,
                  "encryption": "STARTTLS",
                  "authEnabled": false,
                  "fromAddress": "not-an-email"
                }
                """;
        mockMvc.perform(put("/api/email-delivery")
                        .with(adminLogin("admin@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Enter a valid from address."));
    }

    @Test
    void rejectsAuthEnabledWithoutUsername() throws Exception {
        String body = """
                {
                  "host": "smtp.example.com",
                  "port": 587,
                  "encryption": "STARTTLS",
                  "authEnabled": true,
                  "username": "",
                  "password": "secret",
                  "fromAddress": "no-reply@ea-tool.local"
                }
                """;
        mockMvc.perform(put("/api/email-delivery")
                        .with(adminLogin("admin@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is required when authentication is enabled."));
    }

    @Test
    void rejectsAuthEnabledWithoutAnyPassword() throws Exception {
        String body = """
                {
                  "host": "smtp.example.com",
                  "port": 587,
                  "encryption": "STARTTLS",
                  "authEnabled": true,
                  "username": "relay-user",
                  "password": "",
                  "fromAddress": "no-reply@ea-tool.local"
                }
                """;
        mockMvc.perform(put("/api/email-delivery")
                        .with(adminLogin("admin@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password is required when authentication is enabled."));
    }

    @Test
    void viewerCannotSaveConfig() throws Exception {
        mockMvc.perform(put("/api/email-delivery")
                        .with(viewerLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorCannotSaveConfig() throws Exception {
        mockMvc.perform(put("/api/email-delivery")
                        .with(editorLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
