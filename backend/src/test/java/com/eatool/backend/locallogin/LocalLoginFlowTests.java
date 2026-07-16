package com.eatool.backend.locallogin;

import static com.eatool.backend.support.OidcLogins.adminLogin;
import static com.eatool.backend.support.OidcLogins.editorLogin;
import static com.eatool.backend.support.OidcLogins.viewerLogin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.catalogusers.LoginMethod;
import com.eatool.backend.catalogusers.Role;

/**
 * Covers the Local Login vertical (issue #9, ADR-0004): an Admin creates the
 * account via the Catalog Users admin API (no self-signup), the invite token is
 * single-use and expiring, the password is set once, and afterwards the user
 * authenticates with username/password — with the Role authorizing them exactly
 * like an SSO user.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LocalLoginFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogUserRepository catalogUserRepository;

    @Autowired
    private LocalLoginInvitationRepository invitationRepository;

    @Autowired
    private LocalLoginService localLoginService;

    private String createRequest(String name, String email, String role) {
        return String.format("{\"name\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"}", name, email, role);
    }

    @Test
    void adminCreatesLocalUserWithPendingInvite() throws Exception {
        mockMvc.perform(post("/api/catalog-users/local")
                        .with(adminLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest("Percy Partner", "percy@partner.com", "VIEWER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("percy@partner.com"))
                .andExpect(jsonPath("$.loginMethod").value("LOCAL"))
                .andExpect(jsonPath("$.role").value("VIEWER"));

        CatalogUser created = catalogUserRepository.findByEmail("percy@partner.com").orElseThrow();
        assertThat(created.getLoginMethod()).isEqualTo(LoginMethod.LOCAL);
        assertThat(created.getPasswordHash()).isNull();
        assertThat(invitationRepository.findAll()).anySatisfy(invitation -> {
            assertThat(invitation.getCatalogUserId()).isEqualTo(created.getId());
            assertThat(invitation.isUsed()).isFalse();
        });
    }

    @Test
    void nonAdminCannotCreateLocalUser() throws Exception {
        mockMvc.perform(post("/api/catalog-users/local")
                        .with(viewerLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest("No One", "no@one.com", "VIEWER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/catalog-users/local")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest("No One", "no@one.com", "VIEWER")))
                .andExpect(status().isForbidden());

        assertThat(catalogUserRepository.findByEmail("no@one.com")).isEmpty();
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        localLoginService.createLocalUser("First", "dupe@partner.com", "VIEWER");

        mockMvc.perform(post("/api/catalog-users/local")
                        .with(adminLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest("Second", "dupe@partner.com", "EDITOR")))
                .andExpect(status().isConflict());
    }

    @Test
    void invitationDetailsReturnedForValidToken() throws Exception {
        localLoginService.createLocalUser("Val Id", "valid@partner.com", "VIEWER");
        String token = invitationRepository.findAll().get(0).getToken();

        mockMvc.perform(get("/api/local-login/invitations/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("valid@partner.com"))
                .andExpect(jsonPath("$.name").value("Val Id"));
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/local-login/invitations/does-not-exist"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setPasswordActivatesAccountAndSingleUseTokenIsConsumed() throws Exception {
        localLoginService.createLocalUser("Log In", "login@partner.com", "EDITOR");
        String token = invitationRepository.findAll().get(0).getToken();

        mockMvc.perform(post("/api/local-login/set-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"password\":\"correcthorse\"}"))
                .andExpect(status().isNoContent());

        assertThat(catalogUserRepository.findByEmail("login@partner.com").orElseThrow().getPasswordHash())
                .isNotNull();

        // Reusing the same invite link is rejected.
        mockMvc.perform(post("/api/local-login/set-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"password\":\"anotherpass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expiredInvitationIsRejected() throws Exception {
        CatalogUser user = catalogUserRepository.save(
                new CatalogUser("expired@partner.com", "Ex Pired", Role.VIEWER, LoginMethod.LOCAL));
        invitationRepository.save(
                new LocalLoginInvitation(user.getId(), "expired-token", Instant.now().minusSeconds(60)));

        mockMvc.perform(post("/api/local-login/set-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"expired-token\",\"password\":\"correcthorse\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortPasswordIsRejected() throws Exception {
        localLoginService.createLocalUser("Short Pw", "short@partner.com", "VIEWER");
        String token = invitationRepository.findAll().get(0).getToken();

        mockMvc.perform(post("/api/local-login/set-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void localUserAuthenticatesWithUsernameAndPasswordAfterSettingIt() throws Exception {
        localLoginService.createLocalUser("Auth Me", "auth@partner.com", "EDITOR");
        String token = invitationRepository.findAll().get(0).getToken();
        localLoginService.setPassword(token, "correcthorse");

        mockMvc.perform(formLogin("/login").user("auth@partner.com").password("correcthorse"))
                .andExpect(authenticated().withRoles("EDITOR"));

        mockMvc.perform(formLogin("/login").user("auth@partner.com").password("wrongpass"))
                .andExpect(unauthenticated());
    }

    @Test
    void localUserWithoutPasswordCannotAuthenticate() throws Exception {
        localLoginService.createLocalUser("No Pw", "nopw@partner.com", "VIEWER");

        mockMvc.perform(formLogin("/login").user("nopw@partner.com").password("whatever123"))
                .andExpect(unauthenticated());
    }
}
