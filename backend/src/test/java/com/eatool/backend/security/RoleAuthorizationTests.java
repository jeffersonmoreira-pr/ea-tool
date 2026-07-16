package com.eatool.backend.security;

import static com.eatool.backend.support.OidcLogins.editorLogin;
import static com.eatool.backend.support.OidcLogins.viewerLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.eatool.backend.masterdata.BusinessArea;
import com.eatool.backend.masterdata.BusinessAreaRepository;
import com.eatool.backend.masterdata.Department;
import com.eatool.backend.masterdata.DepartmentRepository;
import com.eatool.backend.masterdata.Vendor;
import com.eatool.backend.masterdata.VendorRepository;

/**
 * Covers issue #6: Role-based write authorization on the catalog APIs. A
 * Viewer may read but is denied (403) any create/update/delete on
 * Applications, Vendors, Departments and Business Areas; an Editor may write.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoleAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BusinessAreaRepository businessAreaRepository;

    private static final String[] WRITE_PATHS = {
        "/api/applications",
        "/api/vendors",
        "/api/departments",
        "/api/business-areas"
    };

    @Test
    void viewerCanReadEveryCollection() throws Exception {
        for (String path : WRITE_PATHS) {
            mockMvc.perform(get(path).with(viewerLogin()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void viewerIsForbiddenToCreate() throws Exception {
        for (String path : WRITE_PATHS) {
            mockMvc.perform(post(path)
                            .with(viewerLogin())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Blocked\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void viewerIsForbiddenToUpdate() throws Exception {
        for (String path : WRITE_PATHS) {
            mockMvc.perform(put(path + "/" + UUID.randomUUID())
                            .with(viewerLogin())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Blocked\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void viewerIsForbiddenToDelete() throws Exception {
        for (String path : WRITE_PATHS) {
            mockMvc.perform(delete(path + "/" + UUID.randomUUID())
                            .with(viewerLogin())
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void editorCanCreateMasterData() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Authz Dept\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/vendors")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Authz Vendor\",\"isInternal\":true}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/business-areas")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Authz Area\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void editorCanCreateApplication() throws Exception {
        Vendor vendor = vendorRepository.save(new Vendor("Authz App Vendor", true));
        Department department = departmentRepository.save(new Department("Authz App Dept"));
        BusinessArea businessArea = businessAreaRepository.save(new BusinessArea("Authz App Area"));

        String body = """
                {
                  "name": "Authz App",
                  "description": "desc",
                  "businessOwnerName": "Owner",
                  "techOwnerName": "Tech Owner",
                  "vendorId": "%s",
                  "departmentId": "%s",
                  "businessAreaId": "%s",
                  "lifecycleStatus": "active",
                  "businessFit": 4,
                  "techFit": "high",
                  "pace": "Unclassified",
                  "criticality": "medium",
                  "personalDataHandling": "Unknown",
                  "sensitiveBusinessDataHandling": "Unknown",
                  "informationStatus": "Draft"
                }
                """
                .formatted(vendor.getId(), department.getId(), businessArea.getId());

        mockMvc.perform(post("/api/applications")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
