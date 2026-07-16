package com.eatool.backend;

import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.eatool.backend.applications.Application;
import com.eatool.backend.applications.ApplicationNormalizer;
import com.eatool.backend.applications.ApplicationRepository;
import com.eatool.backend.masterdata.BusinessArea;
import com.eatool.backend.masterdata.BusinessAreaRepository;
import com.eatool.backend.masterdata.Department;
import com.eatool.backend.masterdata.DepartmentRepository;
import com.eatool.backend.masterdata.Vendor;
import com.eatool.backend.masterdata.VendorRepository;

/**
 * Seeds the same demo portfolio previously hardcoded in src/catalog.js's
 * seedCatalog (3 vendors, 3 departments, 3 business areas, 4 applications)
 * so the app has data to look at right after this migration, matching the
 * README's "Validation roteiro" checklist. Only runs when all four
 * repositories are empty, so it never duplicates data on repeated startups.
 */
@Component
public class CatalogDataSeeder implements CommandLineRunner {

    private final VendorRepository vendorRepository;
    private final DepartmentRepository departmentRepository;
    private final BusinessAreaRepository businessAreaRepository;
    private final ApplicationRepository applicationRepository;

    public CatalogDataSeeder(
            VendorRepository vendorRepository,
            DepartmentRepository departmentRepository,
            BusinessAreaRepository businessAreaRepository,
            ApplicationRepository applicationRepository) {
        this.vendorRepository = vendorRepository;
        this.departmentRepository = departmentRepository;
        this.businessAreaRepository = businessAreaRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public void run(String... args) {
        if (vendorRepository.count() > 0
                || departmentRepository.count() > 0
                || businessAreaRepository.count() > 0
                || applicationRepository.count() > 0) {
            return;
        }

        Vendor internalVendor = vendorRepository.save(new Vendor("Internal Digital Team", true));
        Vendor northstarVendor = vendorRepository.save(new Vendor("Northstar Software", false));
        Vendor orbitVendor = vendorRepository.save(new Vendor("Orbit Analytics", false));

        Department financeDepartment = departmentRepository.save(new Department("Finance"));
        Department operationsDepartment = departmentRepository.save(new Department("Operations"));
        Department peopleDepartment = departmentRepository.save(new Department("People"));

        BusinessArea revenueArea = businessAreaRepository.save(new BusinessArea("Revenue Management"));
        BusinessArea fieldArea = businessAreaRepository.save(new BusinessArea("Field Operations"));
        BusinessArea workforceArea = businessAreaRepository.save(new BusinessArea("Workforce Services"));

        applicationRepository.save(buildRevenueHub(northstarVendor.getId(), financeDepartment.getId(), revenueArea.getId()));
        applicationRepository.save(
                buildFieldOpsPortal(internalVendor.getId(), operationsDepartment.getId(), fieldArea.getId()));
        applicationRepository.save(
                buildEmployeeDirectory(internalVendor.getId(), peopleDepartment.getId(), workforceArea.getId()));
        applicationRepository.save(
                buildAnalyticsWorkbench(orbitVendor.getId(), financeDepartment.getId(), revenueArea.getId()));
    }

    private Application buildRevenueHub(UUID vendorId, UUID departmentId, UUID businessAreaId) {
        return buildApplication(
                "Revenue Hub",
                "Portfolio application for revenue planning and invoicing visibility.",
                List.of("RevHub", "Revenue Planning"),
                "https://revenue.example.local",
                "",
                "Maya Chen",
                "maya.chen@example.local",
                "Theo Ramos",
                "theo.ramos@example.local",
                vendorId,
                departmentId,
                businessAreaId,
                "active",
                "",
                "",
                4,
                "high",
                "System of Record",
                "high",
                "Yes",
                "Yes",
                "Verified",
                "2026-07-01");
    }

    private Application buildFieldOpsPortal(UUID vendorId, UUID departmentId, UUID businessAreaId) {
        return buildApplication(
                "Field Ops Portal",
                "Operational portal for coordinating field work and local execution.",
                List.of("Field Portal"),
                "",
                "",
                "Rui Costa",
                "",
                "Ana Silva",
                "",
                vendorId,
                departmentId,
                businessAreaId,
                "active",
                "",
                "",
                3,
                "medium",
                "System of Differentiation",
                "high",
                "No",
                "Yes",
                "Draft",
                "");
    }

    private Application buildEmployeeDirectory(UUID vendorId, UUID departmentId, UUID businessAreaId) {
        return buildApplication(
                "Employee Directory",
                "Reference application for employee lookup and organizational context.",
                List.of("People Directory"),
                "",
                "",
                "Nora Patel",
                "",
                "Ilya Novak",
                "",
                vendorId,
                departmentId,
                businessAreaId,
                "active",
                "",
                "",
                2,
                "medium",
                "System of Record",
                "medium",
                "Yes",
                "No",
                "Needs Review",
                "");
    }

    private Application buildAnalyticsWorkbench(UUID vendorId, UUID departmentId, UUID businessAreaId) {
        return buildApplication(
                "Analytics Workbench",
                "Analytical workspace for management reporting and portfolio exploration.",
                List.of("Analytics Lab"),
                "",
                "https://diagnostics.example.local/analytics",
                "Priya Shah",
                "priya.shah@example.local",
                "Mateo Alves",
                "",
                vendorId,
                departmentId,
                businessAreaId,
                "planned",
                "2026-08-01",
                "",
                5,
                "low",
                "System of Innovation",
                "medium",
                "Unknown",
                "Unknown",
                "Draft",
                "");
    }

    private Application buildApplication(
            String name,
            String description,
            List<String> aliases,
            String applicationUrl,
            String diagnosticUrl,
            String businessOwnerName,
            String businessOwnerEmail,
            String techOwnerName,
            String techOwnerEmail,
            UUID vendorId,
            UUID departmentId,
            UUID businessAreaId,
            String lifecycleStatus,
            String plannedDate,
            String retirementDate,
            int businessFit,
            String techFit,
            String pace,
            String criticality,
            String personalDataHandling,
            String sensitiveBusinessDataHandling,
            String informationStatus,
            String lastVerificationDate) {
        Application application = new Application();
        application.setName(name);
        application.setDescription(description);
        application.setAliases(aliases);
        application.setApplicationUrl(applicationUrl);
        application.setDiagnosticUrl(diagnosticUrl);
        application.setBusinessOwnerName(businessOwnerName);
        application.setBusinessOwnerEmail(businessOwnerEmail);
        application.setTechOwnerName(techOwnerName);
        application.setTechOwnerEmail(techOwnerEmail);
        application.setVendorId(vendorId);
        application.setDepartmentId(departmentId);
        application.setBusinessAreaId(businessAreaId);
        application.setLifecycleStatus(lifecycleStatus);
        application.setPlannedDate(plannedDate);
        application.setRetirementDate(retirementDate);
        application.setBusinessFit(businessFit);
        String businessFitBand = ApplicationNormalizer.deriveBusinessFitBand(businessFit);
        application.setBusinessFitBand(businessFitBand);
        application.setTechFit(techFit);
        application.setTimeClassification(ApplicationNormalizer.deriveTimeClassification(businessFitBand, techFit));
        application.setPace(pace);
        application.setCriticality(criticality);
        application.setPersonalDataHandling(personalDataHandling);
        application.setSensitiveBusinessDataHandling(sensitiveBusinessDataHandling);
        application.setInformationStatus(informationStatus);
        application.setLastVerificationDate(lastVerificationDate);
        return application;
    }
}
