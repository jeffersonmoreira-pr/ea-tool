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

import com.eatool.backend.accessscope.AccessScopeService;
import com.eatool.backend.applications.ApplicationRepository;
import com.eatool.backend.common.BadRequestException;
import com.eatool.backend.common.ConflictException;
import com.eatool.backend.common.NotFoundException;

/**
 * REST API for Business Areas, ported from the frontend's former
 * localStorage-only rules in src/catalog.js
 * (createBusinessArea/updateBusinessArea/deleteBusinessArea). Listing is
 * filtered by the caller's Access Scope (see issue #10).
 */
@RestController
@RequestMapping("/api/business-areas")
public class BusinessAreaController {

    private final BusinessAreaRepository businessAreaRepository;
    private final ApplicationRepository applicationRepository;
    private final AccessScopeService accessScopeService;

    public BusinessAreaController(
            BusinessAreaRepository businessAreaRepository,
            ApplicationRepository applicationRepository,
            AccessScopeService accessScopeService) {
        this.businessAreaRepository = businessAreaRepository;
        this.applicationRepository = applicationRepository;
        this.accessScopeService = accessScopeService;
    }

    @GetMapping
    public List<BusinessArea> list(@AuthenticationPrincipal OidcUser principal) {
        return accessScopeService.filterBusinessAreas(businessAreaRepository.findAll(), principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessArea create(@RequestBody BusinessAreaRequest request) {
        String name = requireUniqueName(request.getName(), null);
        return businessAreaRepository.save(new BusinessArea(name));
    }

    @PutMapping("/{id}")
    public BusinessArea update(@PathVariable UUID id, @RequestBody BusinessAreaRequest request) {
        BusinessArea businessArea = findOrThrow(id);
        String name = requireUniqueName(request.getName(), id);
        businessArea.setName(name);
        return businessAreaRepository.save(businessArea);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        BusinessArea businessArea = findOrThrow(id);
        applicationRepository.findFirstByBusinessAreaId(id).ifPresent(application -> {
            throw new ConflictException("Business Area is in use by Application: " + application.getName() + ".");
        });
        businessAreaRepository.delete(businessArea);
    }

    private BusinessArea findOrThrow(UUID id) {
        return businessAreaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Business Area not found: " + id));
    }

    private String requireUniqueName(String rawName, UUID currentId) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) {
            throw new BadRequestException("Business Area name is required.");
        }
        boolean duplicate = currentId == null
                ? businessAreaRepository.existsByNameIgnoreCase(name)
                : businessAreaRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (duplicate) {
            throw new ConflictException("Business Area name must be unique.");
        }
        return name;
    }
}
