package com.eatool.backend.catalogusers;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
