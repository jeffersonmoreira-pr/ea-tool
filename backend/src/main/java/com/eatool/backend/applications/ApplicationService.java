package com.eatool.backend.applications;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.eatool.backend.applications.ApplicationNormalizer.LifecycleInput;
import com.eatool.backend.applications.ApplicationNormalizer.VerificationInput;
import com.eatool.backend.common.BadRequestException;
import com.eatool.backend.common.ConflictException;
import com.eatool.backend.common.NotFoundException;
import com.eatool.backend.masterdata.BusinessAreaRepository;
import com.eatool.backend.masterdata.DepartmentRepository;
import com.eatool.backend.masterdata.VendorRepository;

/**
 * Application CRUD + validation, faithfully porting the rules in
 * src/catalog.js's normalizeApplicationInput/createApplication/updateApplication.
 */
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final VendorRepository vendorRepository;
    private final DepartmentRepository departmentRepository;
    private final BusinessAreaRepository businessAreaRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            VendorRepository vendorRepository,
            DepartmentRepository departmentRepository,
            BusinessAreaRepository businessAreaRepository) {
        this.applicationRepository = applicationRepository;
        this.vendorRepository = vendorRepository;
        this.departmentRepository = departmentRepository;
        this.businessAreaRepository = businessAreaRepository;
    }

    public List<Application> list() {
        return applicationRepository.findAll();
    }

    public Application create(ApplicationRequest request) {
        Application application = new Application();
        applyValidatedFields(application, request, null);
        return applicationRepository.save(application);
    }

    public Application update(UUID id, ApplicationRequest request) {
        Application application = findOrThrow(id);
        applyValidatedFields(application, request, id);
        return applicationRepository.save(application);
    }

    public void delete(UUID id) {
        Application application = findOrThrow(id);
        applicationRepository.delete(application);
    }

    private Application findOrThrow(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found: " + id));
    }

    private void applyValidatedFields(Application application, ApplicationRequest request, UUID currentId) {
        String name = ApplicationNormalizer.requireName(request.getName(), "Application name is required.");
        boolean duplicate = currentId == null
                ? applicationRepository.existsByNameIgnoreCase(name)
                : applicationRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (duplicate) {
            throw new ConflictException("Application name must be unique.");
        }
        String description =
                ApplicationNormalizer.requireName(request.getDescription(), "Application description is required.");
        String businessOwnerName =
                ApplicationNormalizer.requireName(request.getBusinessOwnerName(), "Business Owner Name is required.");
        String techOwnerName =
                ApplicationNormalizer.requireName(request.getTechOwnerName(), "Tech Owner Name is required.");
        int businessFit = ApplicationNormalizer.normalizeBusinessFit(request.getBusinessFit());
        String techFit = ApplicationNormalizer.normalizeTechFit(request.getTechFit());
        String pace = ApplicationNormalizer.normalizePace(request.getPace());
        String criticality = ApplicationNormalizer.normalizeCriticality(request.getCriticality());
        String personalDataHandling =
                ApplicationNormalizer.normalizeDataHandling(request.getPersonalDataHandling(), "Personal Data Handling");
        String sensitiveBusinessDataHandling = ApplicationNormalizer.normalizeDataHandling(
                request.getSensitiveBusinessDataHandling(), "Sensitive Business Data Handling");
        String businessFitBand = ApplicationNormalizer.deriveBusinessFitBand(businessFit);

        UUID vendorId = requireReference(vendorRepository::existsById, request.getVendorId(), "Vendor");
        UUID departmentId = requireReference(departmentRepository::existsById, request.getDepartmentId(), "Department");
        UUID businessAreaId =
                requireReference(businessAreaRepository::existsById, request.getBusinessAreaId(), "Business Area");

        LifecycleInput lifecycle = ApplicationNormalizer.normalizeLifecycleInput(
                request.getLifecycleStatus(), request.getPlannedDate(), request.getRetirementDate());
        VerificationInput verification = ApplicationNormalizer.normalizeVerificationInput(
                request.getInformationStatus(), request.getLastVerificationDate());

        application.setName(name);
        application.setDescription(description);
        application.setAliases(ApplicationNormalizer.normalizeAliases(request.getAliases()));
        application.setApplicationUrl(ApplicationNormalizer.normalizeText(request.getApplicationUrl()));
        application.setDiagnosticUrl(ApplicationNormalizer.normalizeText(request.getDiagnosticUrl()));
        application.setBusinessOwnerName(businessOwnerName);
        application.setBusinessOwnerEmail(ApplicationNormalizer.normalizeText(request.getBusinessOwnerEmail()));
        application.setTechOwnerName(techOwnerName);
        application.setTechOwnerEmail(ApplicationNormalizer.normalizeText(request.getTechOwnerEmail()));
        application.setVendorId(vendorId);
        application.setDepartmentId(departmentId);
        application.setBusinessAreaId(businessAreaId);
        application.setLifecycleStatus(lifecycle.lifecycleStatus());
        application.setPlannedDate(lifecycle.plannedDate());
        application.setRetirementDate(lifecycle.retirementDate());
        application.setBusinessFit(businessFit);
        application.setBusinessFitBand(businessFitBand);
        application.setTechFit(techFit);
        application.setTimeClassification(ApplicationNormalizer.deriveTimeClassification(businessFitBand, techFit));
        application.setPace(pace);
        application.setCriticality(criticality);
        application.setPersonalDataHandling(personalDataHandling);
        application.setSensitiveBusinessDataHandling(sensitiveBusinessDataHandling);
        application.setInformationStatus(verification.informationStatus());
        application.setLastVerificationDate(verification.lastVerificationDate());
    }

    private UUID requireReference(java.util.function.Predicate<UUID> existsById, String rawId, String label) {
        String idText = ApplicationNormalizer.normalizeText(rawId);
        if (idText.isEmpty()) {
            throw new BadRequestException(label + " is required.");
        }
        UUID id;
        try {
            id = UUID.fromString(idText);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(label + " not found: " + idText);
        }
        if (!existsById.test(id)) {
            throw new BadRequestException(label + " not found: " + idText);
        }
        return id;
    }
}
