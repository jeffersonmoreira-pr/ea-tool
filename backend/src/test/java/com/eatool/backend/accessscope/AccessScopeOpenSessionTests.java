package com.eatool.backend.accessscope;

import static com.eatool.backend.support.OidcLogins.adminLogin;
import static com.eatool.backend.support.OidcLogins.viewerLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.eatool.backend.applications.Application;
import com.eatool.backend.applications.ApplicationRepository;
import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.catalogusers.Role;
import com.eatool.backend.masterdata.BusinessAreaRepository;
import com.eatool.backend.masterdata.Department;
import com.eatool.backend.masterdata.DepartmentRepository;
import com.eatool.backend.masterdata.Vendor;
import com.eatool.backend.masterdata.VendorRepository;

/**
 * Regression guard for issue #10: the Access Scope collections are lazily
 * loaded and the app runs with {@code spring.jpa.open-in-view: false}. This
 * class is deliberately NOT {@code @Transactional}, so each MockMvc request
 * runs with its own session that is already closed by the time the response is
 * serialized — reproducing production behavior. Without a read-only
 * transaction around the scope access, a scoped Viewer's catalog read would
 * fail with LazyInitializationException (HTTP 500). Data is committed and then
 * cleaned up explicitly so it does not leak into the shared test context.
 */
@SpringBootTest(properties = "spring.jpa.open-in-view=false")
@AutoConfigureMockMvc
class AccessScopeOpenSessionTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogUserRepository catalogUserRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BusinessAreaRepository businessAreaRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private UUID userId;
    private UUID applicationId;
    private UUID departmentId;
    private UUID vendorId;
    private UUID businessAreaId;

    @AfterEach
    void cleanUp() {
        if (applicationId != null) {
            applicationRepository.deleteById(applicationId);
        }
        if (userId != null) {
            catalogUserRepository.deleteById(userId);
        }
        if (departmentId != null) {
            departmentRepository.deleteById(departmentId);
        }
        if (businessAreaId != null) {
            businessAreaRepository.deleteById(businessAreaId);
        }
        if (vendorId != null) {
            vendorRepository.deleteById(vendorId);
        }
    }

    @Test
    void scopedViewerReadsCatalogWithOpenSessionInViewDisabled() throws Exception {
        vendorId = vendorRepository.save(new Vendor("OSIV Vendor", false)).getId();
        Department department = departmentRepository.save(new Department("OSIV Finance"));
        departmentId = department.getId();
        businessAreaId = businessAreaRepository.save(new com.eatool.backend.masterdata.BusinessArea("OSIV Corporate"))
                .getId();
        applicationId = saveApplication("OSIV Ledger", vendorId, departmentId, businessAreaId).getId();

        CatalogUser viewer = new CatalogUser("osiv-viewer@example.com", "OSIV Viewer", Role.VIEWER);
        viewer.setAccessScope(Set.of(departmentId), Set.of());
        userId = catalogUserRepository.save(viewer).getId();

        mockMvc.perform(get("/api/applications").with(viewerLogin("osiv-viewer@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='OSIV Ledger')]").exists());
    }

    @Test
    void adminListsCatalogUsersWithScopeWhenOpenSessionInViewDisabled() throws Exception {
        Department department = departmentRepository.save(new Department("OSIV Ops"));
        departmentId = department.getId();
        CatalogUser scoped = new CatalogUser("osiv-scoped@example.com", "OSIV Scoped", Role.VIEWER);
        scoped.setAccessScope(Set.of(departmentId), Set.of());
        userId = catalogUserRepository.save(scoped).getId();

        mockMvc.perform(get("/api/catalog-users").with(adminLogin("osiv-admin@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='osiv-scoped@example.com')].scopedDepartmentIds[0]")
                        .value(departmentId.toString()));
    }

    private Application saveApplication(String name, UUID vendorId, UUID departmentId, UUID businessAreaId) {
        Application application = new Application();
        application.setName(name);
        application.setDescription("desc");
        application.setBusinessOwnerName("Owner");
        application.setTechOwnerName("Tech Owner");
        application.setVendorId(vendorId);
        application.setDepartmentId(departmentId);
        application.setBusinessAreaId(businessAreaId);
        application.setLifecycleStatus("active");
        application.setBusinessFit(3);
        application.setBusinessFitBand("medium");
        application.setTechFit("medium");
        application.setTimeClassification("Tolerate");
        application.setPace("Unclassified");
        application.setCriticality("medium");
        application.setPersonalDataHandling("Unknown");
        application.setSensitiveBusinessDataHandling("Unknown");
        application.setInformationStatus("Draft");
        return applicationRepository.save(application);
    }
}
