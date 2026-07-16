package com.eatool.backend.editpermission;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model for an Edit Permission grant returned by the admin API (issue
 * #11): which record the Editor may edit and when the grant was created.
 */
public record EditPermissionResponse(
        UUID id,
        EditableRecordType recordType,
        UUID recordId,
        UUID catalogUserId,
        Instant grantedAt) {

    public static EditPermissionResponse from(EditPermission permission) {
        return new EditPermissionResponse(
                permission.getId(),
                permission.getRecordType(),
                permission.getRecordId(),
                permission.getCatalogUserId(),
                permission.getGrantedAt());
    }
}
