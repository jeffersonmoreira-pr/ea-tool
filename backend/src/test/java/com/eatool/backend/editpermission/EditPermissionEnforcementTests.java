package com.eatool.backend.editpermission;

import static com.eatool.backend.support.OidcLogins.adminLogin;
import static com.eatool.backend.support.OidcLogins.editorLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
 * Covers Edit Permission enforcement on the catalog write endpoints (issue
 * #11): an Editor may edit/delete only the specific records granted to them
 * (403 otherwise), while an Admin edits any record regardless of grants. Uses
 * Departments as the representative record type; the same enforcement applies
 * to Applications, Vendors and Business Areas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EditPermissionEnforcementTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogUserRepository catalogUserRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EditPermissionRepository editPermissionRepository;

    private static final String EDITOR_EMAIL = "enforce-editor@example.com";

    private CatalogUser persistEditor() {
        return catalogUserRepository.save(new CatalogUser(EDITOR_EMAIL, "Ed Enforce", Role.EDITOR));
    }

    private void grant(UUID recordId, UUID userId) {
        editPermissionRepository.save(new EditPermission(EditableRecordType.DEPARTMENT, recordId, userId));
    }

    @Test
    void editorWithoutPermissionCannotUpdate() throws Exception {
        persistEditor();
        Department department = departmentRepository.save(new Department("NoPerm Update " + UUID.randomUUID()));

        mockMvc.perform(put("/api/departments/" + department.getId())
                        .with(editorLogin(EDITOR_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorWithoutPermissionCannotDelete() throws Exception {
        persistEditor();
        Department department = departmentRepository.save(new Department("NoPerm Delete " + UUID.randomUUID()));

        mockMvc.perform(delete("/api/departments/" + department.getId())
                        .with(editorLogin(EDITOR_EMAIL))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorWithPermissionCanUpdate() throws Exception {
        CatalogUser editor = persistEditor();
        Department department = departmentRepository.save(new Department("Perm Update " + UUID.randomUUID()));
        grant(department.getId(), editor.getId());

        mockMvc.perform(put("/api/departments/" + department.getId())
                        .with(editorLogin(EDITOR_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Editor Renamed " + UUID.randomUUID() + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void editorWithPermissionCanDelete() throws Exception {
        CatalogUser editor = persistEditor();
        Department department = departmentRepository.save(new Department("Perm Delete " + UUID.randomUUID()));
        grant(department.getId(), editor.getId());

        mockMvc.perform(delete("/api/departments/" + department.getId())
                        .with(editorLogin(EDITOR_EMAIL))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void editorCanEditOnlyGrantedRecord() throws Exception {
        CatalogUser editor = persistEditor();
        Department granted = departmentRepository.save(new Department("Granted " + UUID.randomUUID()));
        Department other = departmentRepository.save(new Department("Other " + UUID.randomUUID()));
        grant(granted.getId(), editor.getId());

        mockMvc.perform(put("/api/departments/" + granted.getId())
                        .with(editorLogin(EDITOR_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Granted Renamed " + UUID.randomUUID() + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/departments/" + other.getId())
                        .with(editorLogin(EDITOR_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Other Renamed\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEditsWithoutAnyGrant() throws Exception {
        Department department = departmentRepository.save(new Department("Admin Edit " + UUID.randomUUID()));

        mockMvc.perform(put("/api/departments/" + department.getId())
                        .with(adminLogin("admin@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin Renamed " + UUID.randomUUID() + "\"}"))
                .andExpect(status().isOk());
    }
}
