package com.eatool.backend.editpermission;

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
import jakarta.persistence.UniqueConstraint;

/**
 * An explicit grant, created by an Admin, that lets a specific Catalog User
 * (who must already hold the Editor Role) edit and delete one specific catalog
 * record (issue #11, ADR-0006). Authorization to edit is per record: holding
 * the Editor Role is a prerequisite, and this grant narrows down which records
 * that Editor may actually change.
 *
 * <p>Kept as its own table rather than a collection on each catalog entity so
 * the catalog read models (Application, Vendor, Department, Business Area) stay
 * free of lazy-loaded collections and serialize cleanly under
 * {@code open-in-view=false}.
 */
@Entity
@Table(
        name = "edit_permissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_edit_permission_record_user",
                columnNames = {"record_type", "record_id", "catalog_user_id"}))
public class EditPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 30)
    private EditableRecordType recordType;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    @Column(name = "catalog_user_id", nullable = false)
    private UUID catalogUserId;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;

    protected EditPermission() {
        // JPA
    }

    public EditPermission(EditableRecordType recordType, UUID recordId, UUID catalogUserId) {
        this.recordType = recordType;
        this.recordId = recordId;
        this.catalogUserId = catalogUserId;
        this.grantedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public EditableRecordType getRecordType() {
        return recordType;
    }

    public UUID getRecordId() {
        return recordId;
    }

    public UUID getCatalogUserId() {
        return catalogUserId;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }
}
