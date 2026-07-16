package com.eatool.backend.editpermission;

/**
 * The kind of catalog record an Edit Permission can be granted on (issue #11,
 * ADR-0006). Each value maps to one of the write-protected catalog endpoints:
 * Applications, Vendors, Departments and Business Areas.
 */
public enum EditableRecordType {
    APPLICATION,
    VENDOR,
    DEPARTMENT,
    BUSINESS_AREA
}
