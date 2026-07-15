package com.eatool.backend.catalogusers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auto-provisions a Catalog User on first successful SSO login: creates a
 * Viewer by default, or an Admin when the email is in the configurable seed
 * admin list. A second login for the same email reuses the existing record
 * instead of creating a new one.
 */
@Service
public class CatalogUserProvisioningService {

    private final CatalogUserRepository catalogUserRepository;
    private final SeedAdminsProperties seedAdminsProperties;

    public CatalogUserProvisioningService(
            CatalogUserRepository catalogUserRepository, SeedAdminsProperties seedAdminsProperties) {
        this.catalogUserRepository = catalogUserRepository;
        this.seedAdminsProperties = seedAdminsProperties;
    }

    @Transactional
    public CatalogUser provisionOnLogin(String email, String name) {
        return catalogUserRepository.findByEmail(email)
                .orElseGet(() -> catalogUserRepository.save(new CatalogUser(email, name, resolveRole(email))));
    }

    private Role resolveRole(String email) {
        boolean isSeedAdmin = seedAdminsProperties.getSeedAdmins().stream()
                .anyMatch(seedAdminEmail -> seedAdminEmail.equalsIgnoreCase(email));
        return isSeedAdmin ? Role.ADMIN : Role.VIEWER;
    }
}
