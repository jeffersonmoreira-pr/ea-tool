package com.eatool.backend.masterdata;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A business area an Application supports (see CONTEXT.md). Name
 * uniqueness is case-insensitive and enforced in {@link BusinessAreaController}.
 */
@Entity
@Table(name = "business_areas")
public class BusinessArea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    protected BusinessArea() {
        // JPA
    }

    public BusinessArea(String name) {
        this.name = name;
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
}
