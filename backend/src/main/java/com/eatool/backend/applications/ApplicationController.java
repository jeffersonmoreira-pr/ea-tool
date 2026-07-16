package com.eatool.backend.applications;

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
import com.eatool.backend.editpermission.EditPermissionService;
import com.eatool.backend.editpermission.EditableRecordType;

/**
 * REST API for Applications, ported from the frontend's former
 * localStorage-only rules in src/catalog.js
 * (createApplication/updateApplication/deleteApplication). TIME
 * Classification and Business Fit Band are always recomputed server-side
 * from Business Fit and Tech Fit (see ApplicationNormalizer). Listing is
 * filtered by the caller's Access Scope (see issue #10 / AccessScopeService).
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final AccessScopeService accessScopeService;
    private final EditPermissionService editPermissionService;

    public ApplicationController(
            ApplicationService applicationService,
            AccessScopeService accessScopeService,
            EditPermissionService editPermissionService) {
        this.applicationService = applicationService;
        this.accessScopeService = accessScopeService;
        this.editPermissionService = editPermissionService;
    }

    @GetMapping
    public List<Application> list(@AuthenticationPrincipal OidcUser principal) {
        return accessScopeService.filterApplications(applicationService.list(), principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Application create(@RequestBody ApplicationRequest request) {
        return applicationService.create(request);
    }

    @PutMapping("/{id}")
    public Application update(
            @PathVariable UUID id,
            @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal OidcUser principal) {
        editPermissionService.requireCanEdit(EditableRecordType.APPLICATION, id, principal);
        return applicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal OidcUser principal) {
        editPermissionService.requireCanEdit(EditableRecordType.APPLICATION, id, principal);
        applicationService.delete(id);
    }
}
