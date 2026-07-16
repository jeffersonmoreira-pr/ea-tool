package com.eatool.backend.catalogusers;

/**
 * Request body for changing a Catalog User's Role from the admin screen
 * (see issue #8). The role is validated against the Role enum in the
 * controller so an unknown value yields a 400 instead of a 500.
 */
public class ChangeRoleRequest {

    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
