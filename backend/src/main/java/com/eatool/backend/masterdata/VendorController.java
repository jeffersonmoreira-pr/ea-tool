package com.eatool.backend.masterdata;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eatool.backend.applications.ApplicationRepository;
import com.eatool.backend.common.BadRequestException;
import com.eatool.backend.common.ConflictException;
import com.eatool.backend.common.NotFoundException;
import com.eatool.backend.editpermission.EditPermissionService;
import com.eatool.backend.editpermission.EditableRecordType;

/**
 * REST API for Vendors, ported from the frontend's former localStorage-only
 * rules in src/catalog.js (createVendor/updateVendor/deleteVendor).
 */
@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorRepository vendorRepository;
    private final ApplicationRepository applicationRepository;
    private final EditPermissionService editPermissionService;

    public VendorController(
            VendorRepository vendorRepository,
            ApplicationRepository applicationRepository,
            EditPermissionService editPermissionService) {
        this.vendorRepository = vendorRepository;
        this.applicationRepository = applicationRepository;
        this.editPermissionService = editPermissionService;
    }

    @GetMapping
    public List<Vendor> list() {
        return vendorRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vendor create(@RequestBody VendorRequest request) {
        boolean isInternal = requireInternalFlag(request);
        String name = requireUniqueName(request.getName(), null);
        return vendorRepository.save(new Vendor(name, isInternal));
    }

    @PutMapping("/{id}")
    public Vendor update(
            @PathVariable UUID id,
            @RequestBody VendorRequest request,
            @AuthenticationPrincipal OidcUser principal) {
        editPermissionService.requireCanEdit(EditableRecordType.VENDOR, id, principal);
        boolean isInternal = requireInternalFlag(request);
        Vendor vendor = findOrThrow(id);
        String name = requireUniqueName(request.getName(), id);
        vendor.setName(name);
        vendor.setInternal(isInternal);
        return vendorRepository.save(vendor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal OidcUser principal) {
        editPermissionService.requireCanEdit(EditableRecordType.VENDOR, id, principal);
        Vendor vendor = findOrThrow(id);
        applicationRepository.findFirstByVendorId(id).ifPresent(application -> {
            throw new ConflictException("Vendor is in use by Application: " + application.getName() + ".");
        });
        vendorRepository.delete(vendor);
    }

    private Vendor findOrThrow(UUID id) {
        return vendorRepository.findById(id).orElseThrow(() -> new NotFoundException("Vendor not found: " + id));
    }

    private boolean requireInternalFlag(VendorRequest request) {
        if (request == null || request.getIsInternal() == null) {
            throw new BadRequestException("Vendor internal status is required.");
        }
        return request.getIsInternal();
    }

    private String requireUniqueName(String rawName, UUID currentId) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) {
            throw new BadRequestException("Vendor name is required.");
        }
        boolean duplicate = currentId == null
                ? vendorRepository.existsByNameIgnoreCase(name)
                : vendorRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (duplicate) {
            throw new ConflictException("Vendor name must be unique.");
        }
        return name;
    }
}
