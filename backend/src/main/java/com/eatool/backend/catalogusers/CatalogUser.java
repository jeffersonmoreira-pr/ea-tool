package com.eatool.backend.catalogusers;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * A person recorded locally with a Role so the catalog can authorize what
 * they may do, authenticated either via SSO or Local Login (see CONTEXT.md).
 */
@Entity
@Table(name = "catalog_users")
public class CatalogUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoginMethod loginMethod;

    /**
     * The BCrypt hash of the Local Login password (issue #9). Null for SSO
     * users and for Local Login accounts that have not yet set a password via
     * their invite link. Never exposed by any read model.
     */
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * The Access Scope: the explicit set of Departments and/or Business Areas
     * this user may see (see CONTEXT.md and ADR-0005). Empty means the user
     * sees no Applications, Departments or Business Areas until an Admin
     * assigns a scope; the Admin Role bypasses Access Scope entirely.
     */
    @ElementCollection
    @CollectionTable(
            name = "catalog_user_scoped_departments",
            joinColumns = @JoinColumn(name = "catalog_user_id"))
    @Column(name = "department_id", nullable = false)
    private Set<UUID> scopedDepartmentIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "catalog_user_scoped_business_areas",
            joinColumns = @JoinColumn(name = "catalog_user_id"))
    @Column(name = "business_area_id", nullable = false)
    private Set<UUID> scopedBusinessAreaIds = new HashSet<>();

    protected CatalogUser() {
        // JPA
    }

    public CatalogUser(String email, String name, Role role) {
        this(email, name, role, LoginMethod.SSO);
    }

    public CatalogUser(String email, String name, Role role, LoginMethod loginMethod) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.loginMethod = loginMethod;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LoginMethod getLoginMethod() {
        return loginMethod;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<UUID> getScopedDepartmentIds() {
        return scopedDepartmentIds;
    }

    public Set<UUID> getScopedBusinessAreaIds() {
        return scopedBusinessAreaIds;
    }

    public void setAccessScope(Set<UUID> departmentIds, Set<UUID> businessAreaIds) {
        this.scopedDepartmentIds = new HashSet<>(departmentIds);
        this.scopedBusinessAreaIds = new HashSet<>(businessAreaIds);
    }
}
