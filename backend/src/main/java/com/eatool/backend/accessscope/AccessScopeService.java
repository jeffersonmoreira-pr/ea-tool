package com.eatool.backend.accessscope;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatool.backend.applications.Application;
import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.masterdata.BusinessArea;
import com.eatool.backend.masterdata.Department;
import com.eatool.backend.security.CurrentUserService;

/**
 * Applies Access Scope visibility to catalog listings (issue #10, ADR-0005).
 *
 * <p>Access Scope is an explicit set of Departments and/or Business Areas a
 * Catalog User may see. The rules, following a "deny all by default" stance:
 * <ul>
 *   <li>The Admin Role always sees the full catalog, ignoring Access Scope.</li>
 *   <li>A non-Admin with no Access Scope configured sees no Applications,
 *       Departments or Business Areas.</li>
 *   <li>A non-Admin sees a Department/Business Area only when it is in scope,
 *       and an Application when its Department <em>or</em> Business Area is in
 *       scope.</li>
 *   <li>Vendors are never filtered (handled by their own controller).</li>
 * </ul>
 */
@Service
public class AccessScopeService {

    private final CatalogUserRepository catalogUserRepository;
    private final CurrentUserService currentUserService;

    public AccessScopeService(CatalogUserRepository catalogUserRepository, CurrentUserService currentUserService) {
        this.catalogUserRepository = catalogUserRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<Application> filterApplications(List<Application> applications, Authentication principal) {
        if (seesFullCatalog(principal)) {
            return applications;
        }
        CatalogUser user = currentUser(principal);
        if (user == null) {
            return List.of();
        }
        Set<UUID> departmentIds = user.getScopedDepartmentIds();
        Set<UUID> businessAreaIds = user.getScopedBusinessAreaIds();
        return applications.stream()
                .filter(application -> departmentIds.contains(application.getDepartmentId())
                        || businessAreaIds.contains(application.getBusinessAreaId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Department> filterDepartments(List<Department> departments, Authentication principal) {
        if (seesFullCatalog(principal)) {
            return departments;
        }
        CatalogUser user = currentUser(principal);
        if (user == null) {
            return List.of();
        }
        Set<UUID> departmentIds = user.getScopedDepartmentIds();
        return departments.stream()
                .filter(department -> departmentIds.contains(department.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BusinessArea> filterBusinessAreas(List<BusinessArea> businessAreas, Authentication principal) {
        if (seesFullCatalog(principal)) {
            return businessAreas;
        }
        CatalogUser user = currentUser(principal);
        if (user == null) {
            return List.of();
        }
        Set<UUID> businessAreaIds = user.getScopedBusinessAreaIds();
        return businessAreas.stream()
                .filter(businessArea -> businessAreaIds.contains(businessArea.getId()))
                .toList();
    }

    private boolean seesFullCatalog(Authentication principal) {
        return currentUserService.isAdmin(principal);
    }

    private CatalogUser currentUser(Authentication principal) {
        String email = currentUserService.email(principal);
        if (email == null) {
            return null;
        }
        return catalogUserRepository.findByEmail(email).orElse(null);
    }
}
