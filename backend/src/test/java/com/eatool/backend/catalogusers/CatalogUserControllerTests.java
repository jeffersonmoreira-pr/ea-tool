package com.eatool.backend.catalogusers;

import static com.eatool.backend.support.OidcLogins.adminLogin;
import static com.eatool.backend.support.OidcLogins.editorLogin;
import static com.eatool.backend.support.OidcLogins.viewerLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Covers issue #8: the Admin-only Catalog Users management API. Only Admins
 * may list users and change Roles; Viewers/Editors are denied; an Admin may
 * not change their own Role; unknown roles and users are rejected cleanly.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogUserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogUserRepository catalogUserRepository;

    @Test
    void adminListsUsersWithLoginMethodAndRole() throws Exception {
        catalogUserRepository.save(new CatalogUser("viewer@example.com", "Vera Viewer", Role.VIEWER));

        mockMvc.perform(get("/api/catalog-users").with(adminLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.email=='viewer@example.com')].role").value("VIEWER"))
                .andExpect(jsonPath("$[?(@.email=='viewer@example.com')].loginMethod").value("SSO"));
    }

    @Test
    void adminChangesAnotherUsersRole() throws Exception {
        CatalogUser viewer =
                catalogUserRepository.save(new CatalogUser("promote@example.com", "Percy Promote", Role.VIEWER));

        mockMvc.perform(put("/api/catalog-users/" + viewer.getId() + "/role")
                        .with(adminLogin("admin@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"EDITOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("EDITOR"));

        org.assertj.core.api.Assertions.assertThat(
                        catalogUserRepository.findById(viewer.getId()).orElseThrow().getRole())
                .isEqualTo(Role.EDITOR);
    }

    @Test
    void viewerCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/catalog-users").with(viewerLogin()))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/catalog-users").with(editorLogin()))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotChangeRoles() throws Exception {
        CatalogUser target =
                catalogUserRepository.save(new CatalogUser("target@example.com", "Terry Target", Role.VIEWER));

        mockMvc.perform(put("/api/catalog-users/" + target.getId() + "/role")
                        .with(viewerLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotChangeOwnRole() throws Exception {
        CatalogUser admin =
                catalogUserRepository.save(new CatalogUser("self@example.com", "Sam Self", Role.ADMIN));

        mockMvc.perform(put("/api/catalog-users/" + admin.getId() + "/role")
                        .with(adminLogin("self@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"VIEWER\"}"))
                .andExpect(status().isConflict());

        org.assertj.core.api.Assertions.assertThat(
                        catalogUserRepository.findById(admin.getId()).orElseThrow().getRole())
                .isEqualTo(Role.ADMIN);
    }

    @Test
    void unknownRoleIsRejected() throws Exception {
        CatalogUser target =
                catalogUserRepository.save(new CatalogUser("bad-role@example.com", "Bad Role", Role.VIEWER));

        mockMvc.perform(put("/api/catalog-users/" + target.getId() + "/role")
                        .with(adminLogin("admin@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SUPERUSER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changingRoleOfUnknownUserReturns404() throws Exception {
        mockMvc.perform(put("/api/catalog-users/" + UUID.randomUUID() + "/role")
                        .with(adminLogin("admin@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"EDITOR\"}"))
                .andExpect(status().isNotFound());
    }
}
