package com.eatool.backend.masterdata;

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

import com.eatool.backend.applications.Application;
import com.eatool.backend.applications.ApplicationRepository;

/**
 * Covers the acceptance criteria of issue #4: authenticated CRUD, unique
 * name (409), and the referenced-delete block, now validated server-side.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private BusinessAreaRepository businessAreaRepository;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        int statusCode = mockMvc.perform(get("/api/departments")).andReturn().getResponse().getStatus();
        org.assertj.core.api.Assertions.assertThat(statusCode).isIn(302, 401, 403);
    }

    @Test
    void createListUpdateAndDeleteDepartment() throws Exception {
        mockMvc.perform(get("/api/departments").with(editorLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        String createResponse = mockMvc.perform(post("/api/departments")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Legal\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Legal"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = createResponse.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(put("/api/departments/" + id)
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Legal Affairs\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Legal Affairs"));

        mockMvc.perform(delete("/api/departments/" + id).with(editorLogin()).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Department name is required."));
    }

    @Test
    void createRejectsCaseInsensitiveDuplicateName() throws Exception {
        departmentRepository.save(new Department("Finance"));

        mockMvc.perform(post("/api/departments")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"finance\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Department name must be unique."));
    }

    @Test
    void deleteIsBlockedWhenReferencedByAnApplication() throws Exception {
        Department department = departmentRepository.save(new Department("Marketing"));
        var vendor = vendorRepository.save(new com.eatool.backend.masterdata.Vendor("Acme", true));
        var businessArea = businessAreaRepository.save(new BusinessArea("Growth"));
        Application application = new Application();
        application.setName("Marketing Suite");
        application.setDescription("desc");
        application.setBusinessOwnerName("Owner");
        application.setTechOwnerName("Tech Owner");
        application.setVendorId(vendor.getId());
        application.setDepartmentId(department.getId());
        application.setBusinessAreaId(businessArea.getId());
        application.setLifecycleStatus("active");
        application.setPlannedDate("");
        application.setRetirementDate("");
        application.setBusinessFit(3);
        application.setBusinessFitBand("medium");
        application.setTechFit("medium");
        application.setTimeClassification("Tolerate");
        application.setPace("Unclassified");
        application.setCriticality("medium");
        application.setPersonalDataHandling("Unknown");
        application.setSensitiveBusinessDataHandling("Unknown");
        application.setInformationStatus("Draft");
        application.setLastVerificationDate("");
        applicationRepository.save(application);

        mockMvc.perform(delete("/api/departments/" + department.getId()).with(editorLogin()).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Department is in use by Application: Marketing Suite."));
    }

    @Test
    void deleteOfUnreferencedDepartmentSucceeds() throws Exception {
        Department department = departmentRepository.save(new Department("Unused Department"));

        mockMvc.perform(delete("/api/departments/" + department.getId()).with(editorLogin()).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateOfUnknownDepartmentReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/departments/" + UUID.randomUUID())
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Anything\"}"))
                .andExpect(status().isNotFound());
    }
}
