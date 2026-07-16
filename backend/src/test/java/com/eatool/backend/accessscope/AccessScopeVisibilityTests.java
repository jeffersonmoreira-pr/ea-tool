package com.eatool.backend.accessscope;

import static com.eatool.backend.support.OidcLogins.adminLogin;
import static com.eatool.backend.support.OidcLogins.viewerLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.eatool.backend.applications.Application;
import com.eatool.backend.applications.ApplicationRepository;
import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.catalogusers.Role;
import com.eatool.backend.masterdata.BusinessArea;
import com.eatool.backend.masterdata.BusinessAreaRepository;
import com.eatool.backend.masterdata.Department;
import com.eatool.backend.masterdata.DepartmentRepository;
import com.eatool.backend.masterdata.Vendor;
import com.eatool.backend.masterdata.VendorRepository;

/**
 * Covers issue #10: Access Scope visibility filtering. A non-Admin only sees
 * Applications, Departments and Business Areas within their assigned scope (a
 * user with no scope sees none of them); Vendors are always visible; an Admin
 * always sees the full catalog regardless of scope.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccessScopeVisibilityTests {

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

    @Test
    void scopedViewerSeesOnlyApplicationsInScopedDepartment() throws Exception {
        Vendor vendor = vendorRepository.save(new Vendor("Acme", false));
        Department inScope = departmentRepository.save(new Department("Finance"));
        Department outOfScope = departmentRepository.save(new Department("Operations"));
        BusinessArea businessArea = businessAreaRepository.save(new BusinessArea("Corporate"));
        saveApplication("Ledger", vendor.getId(), inScope.getId(), businessArea.getId());
        saveApplication("Rig Monitor", vendor.getId(), outOfScope.getId(), businessArea.getId());

        saveViewerWithScope("finance@example.com", Set.of(inScope.getId()), Set.of());

        mockMvc.perform(get("/api/applications").with(viewerLogin("finance@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ledger"));
    }

    @Test
    void scopedViewerSeesApplicationInScopedBusinessAreaEvenWhenDepartmentIsNot() throws Exception {
        Vendor vendor = vendorRepository.save(new Vendor("Acme", false));
        Department department = departmentRepository.save(new Department("Operations"));
        BusinessArea inScope = businessAreaRepository.save(new BusinessArea("Reservoirs"));
        saveApplication("Seismic", vendor.getId(), department.getId(), inScope.getId());

        saveViewerWithScope("reservoirs@example.com", Set.of(), Set.of(inScope.getId()));

        mockMvc.perform(get("/api/applications").with(viewerLogin("reservoirs@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Seismic"));
    }

    @Test
    void scopedViewerSeesOnlyScopedDepartmentsAndBusinessAreas() throws Exception {
        Department inScope = departmentRepository.save(new Department("Finance"));
        departmentRepository.save(new Department("Operations"));
        BusinessArea inScopeArea = businessAreaRepository.save(new BusinessArea("Corporate"));
        businessAreaRepository.save(new BusinessArea("Reservoirs"));

        saveViewerWithScope("scoped@example.com", Set.of(inScope.getId()), Set.of(inScopeArea.getId()));

        mockMvc.perform(get("/api/departments").with(viewerLogin("scoped@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Finance"));

        mockMvc.perform(get("/api/business-areas").with(viewerLogin("scoped@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Corporate"));
    }

    @Test
    void viewerWithoutScopeSeesNoApplicationsDepartmentsOrBusinessAreas() throws Exception {
        Vendor vendor = vendorRepository.save(new Vendor("Acme", false));
        Department department = departmentRepository.save(new Department("Finance"));
        BusinessArea businessArea = businessAreaRepository.save(new BusinessArea("Corporate"));
        saveApplication("Ledger", vendor.getId(), department.getId(), businessArea.getId());

        catalogUserRepository.save(new CatalogUser("empty@example.com", "No Scope", Role.VIEWER));

        mockMvc.perform(get("/api/applications").with(viewerLogin("empty@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/departments").with(viewerLogin("empty@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/business-areas").with(viewerLogin("empty@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void vendorsRemainVisibleRegardlessOfScope() throws Exception {
        vendorRepository.save(new Vendor("Scoped Vendor Co", false));
        catalogUserRepository.save(new CatalogUser("empty@example.com", "No Scope", Role.VIEWER));

        mockMvc.perform(get("/api/vendors").with(viewerLogin("empty@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Scoped Vendor Co')]").exists());
    }

    @Test
    void adminSeesFullCatalogRegardlessOfScope() throws Exception {
        Vendor vendor = vendorRepository.save(new Vendor("Scoped Vendor Co", false));
        Department finance = departmentRepository.save(new Department("Scoped Finance"));
        Department operations = departmentRepository.save(new Department("Scoped Operations"));
        BusinessArea businessArea = businessAreaRepository.save(new BusinessArea("Scoped Corporate"));
        saveApplication("Ledger", vendor.getId(), finance.getId(), businessArea.getId());
        saveApplication("Rig Monitor", vendor.getId(), operations.getId(), businessArea.getId());

        // An Admin has no Access Scope configured, yet must still see records
        // across different Departments — proving Admin bypasses Access Scope.
        mockMvc.perform(get("/api/applications").with(adminLogin("admin@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Ledger')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Rig Monitor')]").exists());
        mockMvc.perform(get("/api/departments").with(adminLogin("admin@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Scoped Finance')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Scoped Operations')]").exists());
        mockMvc.perform(get("/api/business-areas").with(adminLogin("admin@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Scoped Corporate')]").exists());
    }

    private void saveViewerWithScope(String email, Set<UUID> departmentIds, Set<UUID> businessAreaIds) {
        CatalogUser user = new CatalogUser(email, "Scoped Viewer", Role.VIEWER);
        user.setAccessScope(departmentIds, businessAreaIds);
        catalogUserRepository.save(user);
    }

    private void saveApplication(String name, UUID vendorId, UUID departmentId, UUID businessAreaId) {
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
        applicationRepository.save(application);
    }
}
