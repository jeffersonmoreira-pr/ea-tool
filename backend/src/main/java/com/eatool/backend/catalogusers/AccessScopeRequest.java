package com.eatool.backend.catalogusers;

import java.util.List;
import java.util.UUID;

/**
 * Body for assigning a Catalog User's Access Scope (issue #10): the set of
 * Department and Business Area ids the user may see. A null or empty list
 * clears that dimension of the scope.
 */
public class AccessScopeRequest {

    private List<UUID> departmentIds;
    private List<UUID> businessAreaIds;

    public List<UUID> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<UUID> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public List<UUID> getBusinessAreaIds() {
        return businessAreaIds;
    }

    public void setBusinessAreaIds(List<UUID> businessAreaIds) {
        this.businessAreaIds = businessAreaIds;
    }
}
