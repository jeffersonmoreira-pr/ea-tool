package com.eatool.backend.applications;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/**
 * A cataloged Application (see CONTEXT.md). References to Vendor,
 * Department and Business Area are stored as plain foreign-key ids (not JPA
 * relationships) to keep the flat request/response shape the frontend
 * already expects. businessFitBand and timeClassification are always
 * server-derived (see ApplicationNormalizer#deriveBusinessFitBand /
 * #deriveTimeClassification) and must never be trusted from client input.
 */
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "application_aliases", joinColumns = @JoinColumn(name = "application_id"))
    @OrderColumn(name = "alias_order")
    @Column(name = "alias")
    private List<String> aliases = new ArrayList<>();

    private String applicationUrl;

    private String diagnosticUrl;

    @Column(nullable = false)
    private String businessOwnerName;

    private String businessOwnerEmail;

    @Column(nullable = false)
    private String techOwnerName;

    private String techOwnerEmail;

    @Column(nullable = false)
    private UUID vendorId;

    @Column(nullable = false)
    private UUID departmentId;

    @Column(nullable = false)
    private UUID businessAreaId;

    @Column(nullable = false)
    private String lifecycleStatus;

    private String plannedDate;

    private String retirementDate;

    @Column(nullable = false)
    private Integer businessFit;

    @Column(nullable = false)
    private String businessFitBand;

    @Column(nullable = false)
    private String techFit;

    @Column(nullable = false)
    private String timeClassification;

    @Column(nullable = false)
    private String pace;

    @Column(nullable = false)
    private String criticality;

    @Column(nullable = false)
    private String personalDataHandling;

    @Column(nullable = false)
    private String sensitiveBusinessDataHandling;

    @Column(nullable = false)
    private String informationStatus;

    private String lastVerificationDate;

    public Application() {
        // Public: constructed directly by ApplicationService/CatalogDataSeeder
        // (all fields set via setters after validation), and by JPA.
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

    public UUID getVendorId() {
        return vendorId;
    }

    public void setVendorId(UUID vendorId) {
        this.vendorId = vendorId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public UUID getBusinessAreaId() {
        return businessAreaId;
    }

    public void setBusinessAreaId(UUID businessAreaId) {
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

    public String getBusinessFitBand() {
        return businessFitBand;
    }

    public void setBusinessFitBand(String businessFitBand) {
        this.businessFitBand = businessFitBand;
    }

    public String getTechFit() {
        return techFit;
    }

    public void setTechFit(String techFit) {
        this.techFit = techFit;
    }

    public String getTimeClassification() {
        return timeClassification;
    }

    public void setTimeClassification(String timeClassification) {
        this.timeClassification = timeClassification;
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
