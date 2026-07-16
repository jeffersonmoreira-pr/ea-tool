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

import com.eatool.backend.masterdata.BusinessArea;
import com.eatool.backend.masterdata.BusinessAreaRepository;
import com.eatool.backend.masterdata.Department;
import com.eatool.backend.masterdata.DepartmentRepository;

/**
 * Covers issue #8: the Admin-only Catalog Users management API. Only Admins
 * may list users and change Roles; Viewers/Editors are denied; an Admin may
 * not change their own Role; unknown roles and users are rejected cleanly.
 * Also covers assigning Access Scope (issue #10).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogUserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogUserRepository catalogUserRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BusinessAreaRepository businessAreaRepository;

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

    @Test
    void adminAssignsAccessScope() throws Exception {
        CatalogUser user =
                catalogUserRepository.save(new CatalogUser("scope-me@example.com", "Scope Me", Role.VIEWER));
        Department department = departmentRepository.save(new Department("Assign Finance"));
        BusinessArea businessArea = businessAreaRepository.save(new BusinessArea("Assign Corporate"));

        mockMvc.perform(put("/api/catalog-users/" + user.getId() + "/access-scope")
                        .with(adminLogin("admin@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentIds\":[\"" + department.getId()
                                + "\"],\"businessAreaIds\":[\"" + businessArea.getId() + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scopedDepartmentIds[0]").value(department.getId().toString()))
                .andExpect(jsonPath("$.scopedBusinessAreaIds[0]").value(businessArea.getId().toString()));

        CatalogUser reloaded = catalogUserRepository.findById(user.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getScopedDepartmentIds())
                .containsExactly(department.getId());
        org.assertj.core.api.Assertions.assertThat(reloaded.getScopedBusinessAreaIds())
                .containsExactly(businessArea.getId());
    }

    @Test
    void assigningScopeWithUnknownDepartmentReturns400() throws Exception {
        CatalogUser user =
                catalogUserRepository.save(new CatalogUser("bad-scope@example.com", "Bad Scope", Role.VIEWER));

        mockMvc.perform(put("/api/catalog-users/" + user.getId() + "/access-scope")
                        .with(adminLogin("admin@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentIds\":[\"" + UUID.randomUUID() + "\"],\"businessAreaIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assigningScopeToUnknownUserReturns404() throws Exception {
        mockMvc.perform(put("/api/catalog-users/" + UUID.randomUUID() + "/access-scope")
                        .with(adminLogin("admin@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentIds\":[],\"businessAreaIds\":[]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void viewerCannotAssignAccessScope() throws Exception {
        CatalogUser user =
                catalogUserRepository.save(new CatalogUser("target-scope@example.com", "Target", Role.VIEWER));

        mockMvc.perform(put("/api/catalog-users/" + user.getId() + "/access-scope")
                        .with(viewerLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentIds\":[],\"businessAreaIds\":[]}"))
                .andExpect(status().isForbidden());
    }
}
