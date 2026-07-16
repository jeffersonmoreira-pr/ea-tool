package com.eatool.backend.masterdata;

/**
 * Request body shape for creating/updating a Business Area.
 */
public class BusinessAreaRequest {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
