package com.eatool.backend.editpermission;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eatool.backend.common.BadRequestException;

/**
 * Admin-only API for managing Edit Permission grants of a Catalog User (issue
 * #11, ADR-0006): list, grant and revoke the specific records an Editor may
 * edit. Nested under {@code /api/catalog-users/**}, which SecurityConfig
 * restricts to Admins (see issue #8).
 */
@RestController
@RequestMapping("/api/catalog-users/{userId}/edit-permissions")
public class EditPermissionController {

    private final EditPermissionService editPermissionService;

    public EditPermissionController(EditPermissionService editPermissionService) {
        this.editPermissionService = editPermissionService;
    }

    @GetMapping
    public List<EditPermissionResponse> list(@PathVariable UUID userId) {
        return editPermissionService.listForUser(userId).stream()
                .map(EditPermissionResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EditPermissionResponse grant(
            @PathVariable UUID userId, @RequestBody EditPermissionRequest request) {
        if (request == null || request.getRecordType() == null) {
            throw new BadRequestException("Record type is required.");
        }
        if (request.getRecordId() == null) {
            throw new BadRequestException("Record id is required.");
        }
        return EditPermissionResponse.from(
                editPermissionService.grant(request.getRecordType(), request.getRecordId(), userId));
    }

    @DeleteMapping("/{recordType}/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            @PathVariable UUID userId,
            @PathVariable EditableRecordType recordType,
            @PathVariable UUID recordId) {
        editPermissionService.revoke(recordType, recordId, userId);
    }
}
