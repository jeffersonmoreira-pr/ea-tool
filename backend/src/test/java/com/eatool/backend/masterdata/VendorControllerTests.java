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
 * Covers the Vendor slice of issue #5: CRUD, isInternal required, unique
 * name (409), and the referenced-delete block validated server-side.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VendorControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BusinessAreaRepository businessAreaRepository;

    @Test
    void createListUpdateAndDeleteVendor() throws Exception {
        String createResponse = mockMvc.perform(post("/api/vendors")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Apex Labs\",\"isInternal\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apex Labs"))
                .andExpect(jsonPath("$.isInternal").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = createResponse.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/vendors").with(editorLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(put("/api/vendors/" + id)
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Apex Laboratories\",\"isInternal\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apex Laboratories"))
                .andExpect(jsonPath("$.isInternal").value(false));

        mockMvc.perform(delete("/api/vendors/" + id).with(editorLogin()).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRequiresInternalFlag() throws Exception {
        mockMvc.perform(post("/api/vendors")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Apex Labs\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vendor internal status is required."));
    }

    @Test
    void createRejectsCaseInsensitiveDuplicateName() throws Exception {
        vendorRepository.save(new Vendor("Northstar Software", false));

        mockMvc.perform(post("/api/vendors")
                        .with(editorLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"northstar software\",\"isInternal\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vendor name must be unique."));
    }

    @Test
    void deleteIsBlockedWhenReferencedByAnApplication() throws Exception {
        Vendor vendor = vendorRepository.save(new Vendor("Referenced Vendor", false));
        Department department = departmentRepository.save(new Department("Ops Dept"));
        BusinessArea businessArea = businessAreaRepository.save(new BusinessArea("Ops Area"));
        Application application = new Application();
        application.setName("Ops Console");
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

        mockMvc.perform(delete("/api/vendors/" + vendor.getId()).with(editorLogin()).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vendor is in use by Application: Ops Console."));
    }
}
