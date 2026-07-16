package com.eatool.backend.masterdata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body shape for creating/updating a Vendor. {@code isInternal} is
 * a boxed Boolean (not a primitive) so a missing value can be distinguished
 * from an explicit {@code false}, matching src/catalog.js's
 * {@code typeof input.isInternal !== "boolean"} check.
 */
public class VendorRequest {

    private String name;

    @JsonProperty("isInternal")
    private Boolean isInternal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(Boolean isInternal) {
        this.isInternal = isInternal;
    }
}
