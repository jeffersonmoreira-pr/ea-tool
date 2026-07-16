package com.eatool.backend.editpermission;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatool.backend.applications.ApplicationRepository;
import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.catalogusers.Role;
import com.eatool.backend.common.BadRequestException;
import com.eatool.backend.common.ForbiddenException;
import com.eatool.backend.common.NotFoundException;
import com.eatool.backend.masterdata.BusinessAreaRepository;
import com.eatool.backend.masterdata.DepartmentRepository;
import com.eatool.backend.masterdata.VendorRepository;

/**
 * Grants, revokes and enforces Edit Permission (issue #11, ADR-0006).
 *
 * <p>Edit Permission is a per-record grant layered on top of the Editor Role:
 * <ul>
 *   <li>The Admin Role may always edit any record, ignoring Edit Permission.</li>
 *   <li>An Editor may edit or delete a record only when granted Edit Permission
 *       on that specific record; otherwise the write is denied with 403.</li>
 *   <li>A Viewer may never edit, even if an Edit Permission was granted by
 *       mistake — the Editor Role stays a prerequisite. (Viewers are also
 *       blocked earlier by the Role rule in SecurityConfig.)</li>
 * </ul>
 */
@Service
public class EditPermissionService {

    private final EditPermissionRepository editPermissionRepository;
    private final CatalogUserRepository catalogUserRepository;
    private final ApplicationRepository applicationRepository;
    private final VendorRepository vendorRepository;
    private final DepartmentRepository departmentRepository;
    private final BusinessAreaRepository businessAreaRepository;

    public EditPermissionService(
            EditPermissionRepository editPermissionRepository,
            CatalogUserRepository catalogUserRepository,
            ApplicationRepository applicationRepository,
            VendorRepository vendorRepository,
            DepartmentRepository departmentRepository,
            BusinessAreaRepository businessAreaRepository) {
        this.editPermissionRepository = editPermissionRepository;
        this.catalogUserRepository = catalogUserRepository;
        this.applicationRepository = applicationRepository;
        this.vendorRepository = vendorRepository;
        this.departmentRepository = departmentRepository;
        this.businessAreaRepository = businessAreaRepository;
    }

    @Transactional
    public EditPermission grant(EditableRecordType recordType, UUID recordId, UUID catalogUserId) {
        requireRecordExists(recordType, recordId);
        CatalogUser user = catalogUserRepository.findById(catalogUserId)
                .orElseThrow(() -> new NotFoundException("Catalog User not found: " + catalogUserId));
        if (user.getRole() != Role.EDITOR) {
            throw new BadRequestException("Edit Permission can only be granted to a Catalog User with the Editor Role.");
        }
        return editPermissionRepository
                .findByRecordTypeAndRecordIdAndCatalogUserId(recordType, recordId, catalogUserId)
                .orElseGet(() -> editPermissionRepository.save(
                        new EditPermission(recordType, recordId, catalogUserId)));
    }

    @Transactional
    public void revoke(EditableRecordType recordType, UUID recordId, UUID catalogUserId) {
        if (!catalogUserRepository.existsById(catalogUserId)) {
            throw new NotFoundException("Catalog User not found: " + catalogUserId);
        }
        EditPermission grant = editPermissionRepository
                .findByRecordTypeAndRecordIdAndCatalogUserId(recordType, recordId, catalogUserId)
                .orElseThrow(() -> new NotFoundException("Edit Permission not found for this record and user."));
        editPermissionRepository.delete(grant);
    }

    @Transactional(readOnly = true)
    public List<EditPermission> listForUser(UUID catalogUserId) {
        if (!catalogUserRepository.existsById(catalogUserId)) {
            throw new NotFoundException("Catalog User not found: " + catalogUserId);
        }
        return editPermissionRepository.findByCatalogUserIdOrderByGrantedAt(catalogUserId);
    }

    @Transactional(readOnly = true)
    public boolean canEdit(EditableRecordType recordType, UUID recordId, OidcUser principal) {
        if (isAdmin(principal)) {
            return true;
        }
        CatalogUser user = currentUser(principal);
        if (user == null || user.getRole() != Role.EDITOR) {
            return false;
        }
        return editPermissionRepository
                .existsByRecordTypeAndRecordIdAndCatalogUserId(recordType, recordId, user.getId());
    }

    @Transactional(readOnly = true)
    public void requireCanEdit(EditableRecordType recordType, UUID recordId, OidcUser principal) {
        if (!canEdit(recordType, recordId, principal)) {
            throw new ForbiddenException("You do not have Edit Permission for this record.");
        }
    }

    private void requireRecordExists(EditableRecordType recordType, UUID recordId) {
        if (recordId == null) {
            throw new BadRequestException("Record id is required.");
        }
        Predicate<UUID> existsById = switch (recordType) {
            case APPLICATION -> applicationRepository::existsById;
            case VENDOR -> vendorRepository::existsById;
            case DEPARTMENT -> departmentRepository::existsById;
            case BUSINESS_AREA -> businessAreaRepository::existsById;
        };
        if (!existsById.test(recordId)) {
            throw new BadRequestException(label(recordType) + " not found: " + recordId);
        }
    }

    private String label(EditableRecordType recordType) {
        return switch (recordType) {
            case APPLICATION -> "Application";
            case VENDOR -> "Vendor";
            case DEPARTMENT -> "Department";
            case BUSINESS_AREA -> "Business Area";
        };
    }

    private boolean isAdmin(OidcUser principal) {
        if (principal == null) {
            return false;
        }
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private CatalogUser currentUser(OidcUser principal) {
        if (principal == null || principal.getEmail() == null) {
            return null;
        }
        return catalogUserRepository.findByEmail(principal.getEmail()).orElse(null);
    }
}
