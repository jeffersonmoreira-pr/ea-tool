package com.eatool.backend.masterdata;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static com.eatool.backend.support.OidcLogins.editorLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.eatool.backend.applications.Application;
import com.eatool.backend.applications.ApplicationRepository;

/**
 * Covers the Business Area slice of issue #5: CRUD, unique name (409), and
 * the referenced-delete block validated server-side.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BusinessAreaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusinessAreaRepository businessAreaRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void createListUpdateAndDeleteBusinessArea() throws Exception {
        String createResponse = mockMvc.perform(post("/api/business-areas")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Customer Growth\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Customer Growth"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = createResponse.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/business-areas").with(editorLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(put("/api/business-areas/" + id)
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Growth & Retention\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Growth & Retention"));

        mockMvc.perform(delete("/api/business-areas/" + id).with(editorLogin()).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/business-areas")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Business Area name is required."));
    }

    @Test
    void createRejectsCaseInsensitiveDuplicateName() throws Exception {
        businessAreaRepository.save(new BusinessArea("Revenue Management"));

        mockMvc.perform(post("/api/business-areas")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"revenue management\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Business Area name must be unique."));
    }

    @Test
    void deleteIsBlockedWhenReferencedByAnApplication() throws Exception {
        BusinessArea businessArea = businessAreaRepository.save(new BusinessArea("Referenced Area"));
        Vendor vendor = vendorRepository.save(new Vendor("Area Vendor", true));
        Department department = departmentRepository.save(new Department("Area Dept"));
        Application application = new Application();
        application.setName("Area App");
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

        mockMvc.perform(delete("/api/business-areas/" + businessArea.getId()).with(editorLogin()).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Business Area is in use by Application: Area App."));
    }
}
