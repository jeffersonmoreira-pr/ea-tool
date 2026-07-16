package com.eatool.backend.applications;

import java.util.List;

/**
 * Request body shape for creating/updating an Application. Deliberately
 * excludes {@code id}, {@code businessFitBand} and {@code timeClassification}:
 * those are always server-derived (see ApplicationNormalizer).
 */
public class ApplicationRequest {

    private String name;
    private String description;
    private List<String> aliases;
    private String applicationUrl;
    private String diagnosticUrl;
    private String businessOwnerName;
    private String businessOwnerEmail;
    private String techOwnerName;
    private String techOwnerEmail;
    private String vendorId;
    private String departmentId;
    private String businessAreaId;
    private String lifecycleStatus;
    private String plannedDate;
    private String retirementDate;
    private Integer businessFit;
    private String techFit;
    private String pace;
    private String criticality;
    private String personalDataHandling;
    private String sensitiveBusinessDataHandling;
    private String informationStatus;
    private String lastVerificationDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getDiagnosticUrl() {
        return diagnosticUrl;
    }

    public void setDiagnosticUrl(String diagnosticUrl) {
        this.diagnosticUrl = diagnosticUrl;
    }

    public String getBusinessOwnerName() {
        return businessOwnerName;
    }

    public void setBusinessOwnerName(String businessOwnerName) {
        this.businessOwnerName = businessOwnerName;
    }

    public String getBusinessOwnerEmail() {
        return businessOwnerEmail;
    }

    public void setBusinessOwnerEmail(String businessOwnerEmail) {
        this.businessOwnerEmail = businessOwnerEmail;
    }

    public String getTechOwnerName() {
        return techOwnerName;
    }

    public void setTechOwnerName(String techOwnerName) {
        this.techOwnerName = techOwnerName;
    }

    public String getTechOwnerEmail() {
        return techOwnerEmail;
    }

    public void setTechOwnerEmail(String techOwnerEmail) {
        this.techOwnerEmail = techOwnerEmail;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getBusinessAreaId() {
        return businessAreaId;
    }

    public void setBusinessAreaId(String businessAreaId) {
        this.businessAreaId = businessAreaId;
    }

    public String getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(String lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(String plannedDate) {
        this.plannedDate = plannedDate;
    }

    public String getRetirementDate() {
        return retirementDate;
    }

    public void setRetirementDate(String retirementDate) {
        this.retirementDate = retirementDate;
    }

    public Integer getBusinessFit() {
        return businessFit;
    }

    public void setBusinessFit(Integer businessFit) {
        this.businessFit = businessFit;
    }

    public String getTechFit() {
        return techFit;
    }

    public void setTechFit(String techFit) {
        this.techFit = techFit;
    }

    public String getPace() {
        return pace;
    }

    public void setPace(String pace) {
        this.pace = pace;
    }

    public String getCriticality() {
        return criticality;
    }

    public void setCriticality(String criticality) {
        this.criticality = criticality;
    }

    public String getPersonalDataHandling() {
        return personalDataHandling;
    }

    public void setPersonalDataHandling(String personalDataHandling) {
        this.personalDataHandling = personalDataHandling;
    }

    public String getSensitiveBusinessDataHandling() {
        return sensitiveBusinessDataHandling;
    }

    public void setSensitiveBusinessDataHandling(String sensitiveBusinessDataHandling) {
        this.sensitiveBusinessDataHandling = sensitiveBusinessDataHandling;
    }

    public String getInformationStatus() {
        return informationStatus;
    }

    public void setInformationStatus(String informationStatus) {
        this.informationStatus = informationStatus;
    }

    public String getLastVerificationDate() {
        return lastVerificationDate;
    }

    public void setLastVerificationDate(String lastVerificationDate) {
        this.lastVerificationDate = lastVerificationDate;
    }
}
