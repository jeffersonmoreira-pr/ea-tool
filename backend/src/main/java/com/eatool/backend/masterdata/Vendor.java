package com.eatool.backend.masterdata;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A vendor supplying or hosting an Application (see CONTEXT.md: Vendor,
 * Internal Vendor). Name uniqueness is case-insensitive and enforced in
 * {@link VendorController}.
 */
@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @JsonProperty("isInternal")
    private boolean isInternal;

    protected Vendor() {
        // JPA
    }

    public Vendor(String name, boolean isInternal) {
        this.name = name;
        this.isInternal = isInternal;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }
}
