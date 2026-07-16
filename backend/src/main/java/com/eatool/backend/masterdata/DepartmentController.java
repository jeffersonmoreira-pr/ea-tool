package com.eatool.backend.masterdata;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
import com.eatool.backend.editpermission.EditPermissionService;
import com.eatool.backend.editpermission.EditableRecordType;

/**
 * REST API for Departments, ported from the frontend's former
 * localStorage-only rules in src/catalog.js
 * (createDepartment/updateDepartment/deleteDepartment). Any authenticated
 * Catalog User may read (filtered by Access Scope, see issue #10); creating,
 * editing or deleting requires an Editor or Admin Role, enforced centrally in
 * SecurityConfig (see issue #6).
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final ApplicationRepository applicationRepository;
    private final AccessScopeService accessScopeService;
    private final EditPermissionService editPermissionService;

    public DepartmentController(
            DepartmentRepository departmentRepository,
            ApplicationRepository applicationRepository,
            AccessScopeService accessScopeService,
            EditPermissionService editPermissionService) {
        this.departmentRepository = departmentRepository;
        this.applicationRepository = applicationRepository;
        this.accessScopeService = accessScopeService;
        this.editPermissionService = editPermissionService;
    }

    @GetMapping
    public List<Department> list(Authentication principal) {
        return accessScopeService.filterDepartments(departmentRepository.findAll(), principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Department create(@RequestBody DepartmentRequest request) {
        String name = requireUniqueName(request.getName(), null);
        return departmentRepository.save(new Department(name));
    }

    @PutMapping("/{id}")
    public Department update(
            @PathVariable UUID id,
            @RequestBody DepartmentRequest request,
            Authentication principal) {
        editPermissionService.requireCanEdit(EditableRecordType.DEPARTMENT, id, principal);
        Department department = findOrThrow(id);
        String name = requireUniqueName(request.getName(), id);
        department.setName(name);
        return departmentRepository.save(department);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication principal) {
        editPermissionService.requireCanEdit(EditableRecordType.DEPARTMENT, id, principal);
        Department department = findOrThrow(id);
        applicationRepository.findFirstByDepartmentId(id).ifPresent(application -> {
            throw new ConflictException("Department is in use by Application: " + application.getName() + ".");
        });
        departmentRepository.delete(department);
    }

    private Department findOrThrow(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found: " + id));
    }

    private String requireUniqueName(String rawName, UUID currentId) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) {
            throw new BadRequestException("Department name is required.");
        }
        boolean duplicate = currentId == null
                ? departmentRepository.existsByNameIgnoreCase(name)
                : departmentRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (duplicate) {
            throw new ConflictException("Department name must be unique.");
        }
        return name;
    }
}
