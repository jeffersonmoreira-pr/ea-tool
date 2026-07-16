package com.eatool.backend.editpermission;

import static com.eatool.backend.support.OidcLogins.adminLogin;
import static com.eatool.backend.support.OidcLogins.editorLogin;
import static com.eatool.backend.support.OidcLogins.viewerLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.catalogusers.Role;
import com.eatool.backend.masterdata.Department;
import com.eatool.backend.masterdata.DepartmentRepository;

/**
 * Covers the Admin-only Edit Permission management API (issue #11, ADR-0006):
 * an Admin grants, lists and revokes the specific records an Editor may edit.
 * Only Admins may reach it (nested under /api/catalog-users/**); grants are
 * rejected for non-Editors and unknown records/users.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EditPermissionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogUserRepository catalogUserRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EditPermissionRepository editPermissionRepository;

    private CatalogUser editor() {
        return catalogUserRepository.save(
                new CatalogUser("editor-" + UUID.randomUUID() + "@example.com", "Ed Editor", Role.EDITOR));
    }

    @Test
    void adminGrantsEditPermissionToEditor() throws Exception {
        CatalogUser user = editor();
        Department department = departmentRepository.save(new Department("Grant Dept " + UUID.randomUUID()));

        mockMvc.perform(post("/api/catalog-users/" + user.getId() + "/edit-permissions")
                        .with(adminLogin("admin@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordType\":\"DEPARTMENT\",\"recordId\":\"" + department.getId() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recordType").value("DEPARTMENT"))
                .andExpect(jsonPath("$.recordId").value(department.getId().toString()))
                .andExpect(jsonPath("$.catalogUserId").value(user.getId().toString()));

        org.assertj.core.api.Assertions.assertThat(
                        editPermissionRepository.existsByRecordTypeAndRecordIdAndCatalogUserId(
                                EditableRecordType.DEPARTMENT, department.getId(), user.getId()))
                .isTrue();
    }

    @Test
    void grantingTwiceIsIdempotent() throws Exception {
        CatalogUser user = editor();
        Department department = departmentRepository.save(new Department("Idem Dept " + UUID.randomUUID()));
        String body = "{\"recordType\":\"DEPARTMENT\",\"recordId\":\"" + department.getId() + "\"}";

        mockMvc.perform(post("/api/catalog-users/" + user.getId() + "/edit-permissions")
                        .with(adminLogin()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/catalog-users/" + user.getId() + "/edit-permissions")
                        .with(adminLogin()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        org.assertj.core.api.Assertions.assertThat(
                        editPermissionRepository.findByCatalogUserIdOrderByGrantedAt(user.getId()))
                .hasSize(1);
    }

    @Test
    void grantingToNonEditorIsRejected() throws Exception {
        CatalogUser viewer =
                catalogUserRepository.save(new CatalogUser("viewer-grant@example.com", "Vic Viewer", Role.VIEWER));
        Department department = departmentRepository.save(new Department("NonEditor Dept " + UUID.randomUUID()));

        mockMvc.perform(post("/api/catalog-users/" + viewer.getId() + "/edit-permissions")
                        .with(adminLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordType\":\"DEPARTMENT\",\"recordId\":\"" + department.getId() + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void grantingUnknownRecordIsRejected() throws Exception {
        CatalogUser user = editor();

        mockMvc.perform(post("/api/catalog-users/" + user.getId() + "/edit-permissions")
                        .with(adminLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordType\":\"DEPARTMENT\",\"recordId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void grantingToUnknownUserReturns404() throws Exception {
        Department department = departmentRepository.save(new Department("Ghost Dept " + UUID.randomUUID()));

        mockMvc.perform(post("/api/catalog-users/" + UUID.randomUUID() + "/edit-permissions")
                        .with(adminLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordType\":\"DEPARTMENT\",\"recordId\":\"" + department.getId() + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminRevokesEditPermission() throws Exception {
        CatalogUser user = editor();
        Department department = departmentRepository.save(new Department("Revoke Dept " + UUID.randomUUID()));
        editPermissionRepository.save(
                new EditPermission(EditableRecordType.DEPARTMENT, department.getId(), user.getId()));

        mockMvc.perform(delete("/api/catalog-users/" + user.getId()
                        + "/edit-permissions/DEPARTMENT/" + department.getId())
                        .with(adminLogin())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(
                        editPermissionRepository.existsByRecordTypeAndRecordIdAndCatalogUserId(
                                EditableRecordType.DEPARTMENT, department.getId(), user.getId()))
                .isFalse();
    }

    @Test
    void revokingUnknownGrantReturns404() throws Exception {
        CatalogUser user = editor();

        mockMvc.perform(delete("/api/catalog-users/" + user.getId()
                        + "/edit-permissions/DEPARTMENT/" + UUID.randomUUID())
                        .with(adminLogin())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminListsGrantsForUser() throws Exception {
        CatalogUser user = editor();
        Department department = departmentRepository.save(new Department("List Dept " + UUID.randomUUID()));
        editPermissionRepository.save(
                new EditPermission(EditableRecordType.DEPARTMENT, department.getId(), user.getId()));

        mockMvc.perform(get("/api/catalog-users/" + user.getId() + "/edit-permissions").with(adminLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.recordId=='" + department.getId() + "')].recordType").value("DEPARTMENT"));
    }

    @Test
    void viewerCannotManageEditPermissions() throws Exception {
        CatalogUser user = editor();

        mockMvc.perform(get("/api/catalog-users/" + user.getId() + "/edit-permissions").with(viewerLogin()))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorCannotManageEditPermissions() throws Exception {
        CatalogUser user = editor();

        mockMvc.perform(get("/api/catalog-users/" + user.getId() + "/edit-permissions").with(editorLogin()))
                .andExpect(status().isForbidden());
    }
}
