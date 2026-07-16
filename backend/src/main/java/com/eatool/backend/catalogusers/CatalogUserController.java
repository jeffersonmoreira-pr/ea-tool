package com.eatool.backend.catalogusers;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eatool.backend.common.BadRequestException;
import com.eatool.backend.common.ConflictException;
import com.eatool.backend.common.NotFoundException;
import com.eatool.backend.locallogin.CreateLocalUserRequest;
import com.eatool.backend.locallogin.LocalLoginService;
import com.eatool.backend.masterdata.BusinessAreaRepository;
import com.eatool.backend.masterdata.DepartmentRepository;
import com.eatool.backend.security.CurrentUserService;

/**
 * Admin-only API backing the Catalog Users management screen (see issue #8):
 * list every provisioned user with their login method and Role, and change
 * another user's Role. Access is restricted to Admins centrally in
 * SecurityConfig (hasRole("ADMIN") on /api/catalog-users/**).
 */
@RestController
@RequestMapping("/api/catalog-users")
public class CatalogUserController {

    private final CatalogUserRepository catalogUserRepository;
    private final DepartmentRepository departmentRepository;
    private final BusinessAreaRepository businessAreaRepository;
    private final CurrentUserService currentUserService;
    private final LocalLoginService localLoginService;

    public CatalogUserController(
            CatalogUserRepository catalogUserRepository,
            DepartmentRepository departmentRepository,
            BusinessAreaRepository businessAreaRepository,
            CurrentUserService currentUserService,
            LocalLoginService localLoginService) {
        this.catalogUserRepository = catalogUserRepository;
        this.departmentRepository = departmentRepository;
        this.businessAreaRepository = businessAreaRepository;
        this.currentUserService = currentUserService;
        this.localLoginService = localLoginService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<CatalogUserResponse> list() {
        return catalogUserRepository.findAll().stream()
                .sorted(Comparator.comparing(CatalogUser::getCreatedAt))
                .map(CatalogUserResponse::from)
                .toList();
    }

    @PostMapping("/local")
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogUserResponse createLocalUser(@RequestBody CreateLocalUserRequest request) {
        CatalogUser user = localLoginService.createLocalUser(
                request.getName(), request.getEmail(), request.getRole());
        return CatalogUserResponse.from(user);
    }

    @PutMapping("/{id}/role")
    @Transactional
    public CatalogUserResponse changeRole(
            @PathVariable UUID id,
            @RequestBody ChangeRoleRequest request,
            Authentication currentUser) {
        Role newRole = parseRole(request.getRole());
        CatalogUser user = catalogUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Catalog User not found: " + id));

        if (isSelf(user, currentUser)) {
            throw new ConflictException("You cannot change your own Role.");
        }

        user.setRole(newRole);
        return CatalogUserResponse.from(catalogUserRepository.save(user));
    }

    @PutMapping("/{id}/access-scope")
    @Transactional
    public CatalogUserResponse assignAccessScope(
            @PathVariable UUID id, @RequestBody AccessScopeRequest request) {
        CatalogUser user = catalogUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Catalog User not found: " + id));

        Set<UUID> departmentIds =
                validateReferences(request.getDepartmentIds(), departmentRepository::existsById, "Department");
        Set<UUID> businessAreaIds =
                validateReferences(request.getBusinessAreaIds(), businessAreaRepository::existsById, "Business Area");

        user.setAccessScope(departmentIds, businessAreaIds);
        return CatalogUserResponse.from(catalogUserRepository.save(user));
    }

    private Set<UUID> validateReferences(
            List<UUID> rawIds, java.util.function.Predicate<UUID> existsById, String label) {
        Set<UUID> ids = new LinkedHashSet<>();
        if (rawIds == null) {
            return ids;
        }
        for (UUID id : rawIds) {
            if (id == null) {
                throw new BadRequestException(label + " id is required.");
            }
            if (!existsById.test(id)) {
                throw new BadRequestException(label + " not found: " + id);
            }
            ids.add(id);
        }
        return ids;
    }

    private boolean isSelf(CatalogUser user, Authentication currentUser) {
        String currentEmail = currentUserService.email(currentUser);
        return currentEmail != null && currentEmail.equalsIgnoreCase(user.getEmail());
    }

    private Role parseRole(String rawRole) {
        String value = rawRole == null ? "" : rawRole.trim();
        if (value.isEmpty()) {
            throw new BadRequestException("Role is required.");
        }
        try {
            return Role.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw new BadRequestException("Role must be VIEWER, EDITOR, or ADMIN.");
        }
    }
}
