package com.eatool.backend.applications;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static com.eatool.backend.support.OidcLogins.editorLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.eatool.backend.masterdata.Vendor;
import com.eatool.backend.masterdata.VendorRepository;

/**
 * Covers the Application slice of issue #5: CRUD, FK validation, unique
 * name (409), required fields (400), and server-derived TIME
 * Classification/Business Fit Band.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApplicationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BusinessAreaRepository businessAreaRepository;

    private String createApplicationRequest(UUID vendorId, UUID departmentId, UUID businessAreaId, int businessFit, String techFit) {
        return """
                {
                  "name": "Test App %s",
                  "description": "desc",
                  "aliases": ["Alias One"],
                  "businessOwnerName": "Owner",
                  "techOwnerName": "Tech Owner",
                  "vendorId": "%s",
                  "departmentId": "%s",
                  "businessAreaId": "%s",
                  "lifecycleStatus": "active",
                  "businessFit": %d,
                  "techFit": "%s",
                  "pace": "Unclassified",
                  "criticality": "medium",
                  "personalDataHandling": "Unknown",
                  "sensitiveBusinessDataHandling": "Unknown",
                  "informationStatus": "Draft"
                }
                """
                .formatted(UUID.randomUUID(), vendorId, departmentId, businessAreaId, businessFit, techFit);
    }

    private UUID[] seedReferences() {
        Vendor vendor = vendorRepository.save(new Vendor("Test Vendor", true));
        Department department = departmentRepository.save(new Department("Test Department"));
        BusinessArea businessArea = businessAreaRepository.save(new BusinessArea("Test Area"));
        return new UUID[] {vendor.getId(), department.getId(), businessArea.getId()};
    }

    @Test
    void createListUpdateAndDeleteApplication() throws Exception {
        UUID[] refs = seedReferences();

        String createResponse = mockMvc.perform(post("/api/applications")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createApplicationRequest(refs[0], refs[1], refs[2], 4, "high")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessFitBand").value("high"))
                .andExpect(jsonPath("$.timeClassification").value("Invest"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = createResponse.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/applications").with(editorLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(put("/api/applications/" + id)
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createApplicationRequest(refs[0], refs[1], refs[2], 1, "low")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessFitBand").value("low"))
                .andExpect(jsonPath("$.timeClassification").value("Eliminate"));

        mockMvc.perform(delete("/api/applications/" + id).with(editorLogin()).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRejectsMissingRequiredField() throws Exception {
        UUID[] refs = seedReferences();
        String body = """
                {
                  "name": "",
                  "description": "desc",
                  "businessOwnerName": "Owner",
                  "techOwnerName": "Tech Owner",
                  "vendorId": "%s",
                  "departmentId": "%s",
                  "businessAreaId": "%s",
                  "businessFit": 3,
                  "techFit": "medium"
                }
                """
                .formatted(refs[0], refs[1], refs[2]);

        mockMvc.perform(post("/api/applications")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application name is required."));
    }

    @Test
    void createRejectsUnknownVendorReference() throws Exception {
        UUID[] refs = seedReferences();
        String body = createApplicationRequest(UUID.randomUUID(), refs[1], refs[2], 3, "medium");

        mockMvc.perform(post("/api/applications")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsCaseInsensitiveDuplicateName() throws Exception {
        UUID[] refs = seedReferences();
        String body = """
                {
                  "name": "Duplicate App",
                  "description": "desc",
                  "businessOwnerName": "Owner",
                  "techOwnerName": "Tech Owner",
                  "vendorId": "%s",
                  "departmentId": "%s",
                  "businessAreaId": "%s",
                  "businessFit": 3,
                  "techFit": "medium"
                }
                """
                .formatted(refs[0], refs[1], refs[2]);

        mockMvc.perform(post("/api/applications")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String duplicateBody = body.replace("Duplicate App", "duplicate app");
        mockMvc.perform(post("/api/applications")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Application name must be unique."));
    }

    @Test
    void plannedLifecycleRequiresPlannedDate() throws Exception {
        UUID[] refs = seedReferences();
        String body = """
                {
                  "name": "Planned App",
                  "description": "desc",
                  "businessOwnerName": "Owner",
                  "techOwnerName": "Tech Owner",
                  "vendorId": "%s",
                  "departmentId": "%s",
                  "businessAreaId": "%s",
                  "lifecycleStatus": "planned",
                  "businessFit": 3,
                  "techFit": "medium"
                }
                """
                .formatted(refs[0], refs[1], refs[2]);

        mockMvc.perform(post("/api/applications")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Planned Date is required for planned Applications."));
    }
}
