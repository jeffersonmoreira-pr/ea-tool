package com.eatool.backend.editpermission;

import java.util.UUID;

/**
 * Body for granting an Edit Permission to a Catalog User (issue #11): which
 * record type and which specific record the Editor may edit.
 */
public class EditPermissionRequest {

    private EditableRecordType recordType;
    private UUID recordId;

    public EditableRecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(EditableRecordType recordType) {
        this.recordType = recordType;
    }

    public UUID getRecordId() {
        return recordId;
    }

    public void setRecordId(UUID recordId) {
        this.recordId = recordId;
    }
}
