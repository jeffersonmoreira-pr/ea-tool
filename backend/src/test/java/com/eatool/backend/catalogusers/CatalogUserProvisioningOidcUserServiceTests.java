package com.eatool.backend.catalogusers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({CatalogUserProvisioningService.class, CatalogUserProvisioningOidcUserServiceTests.TestSeedAdminsConfig.class})
class CatalogUserProvisioningOidcUserServiceTests {

    @Autowired
    private CatalogUserProvisioningService provisioningService;

    @Autowired
    private CatalogUserRepository catalogUserRepository;

    @Test
    void firstLoginCreatesViewerByDefault() {
        CatalogUser created = provisioningService.provisionOnLogin("new.user@example.com", "New User");

        assertThat(created.getRole()).isEqualTo(Role.VIEWER);
        assertThat(catalogUserRepository.findByEmail("new.user@example.com")).isPresent();
    }

    @Test
    void secondLoginReusesExistingCatalogUser() {
        CatalogUser first = provisioningService.provisionOnLogin("repeat.user@example.com", "Repeat User");
        CatalogUser second = provisioningService.provisionOnLogin("repeat.user@example.com", "Repeat User");

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(catalogUserRepository.count()).isEqualTo(1);
    }

    @Test
    void seedAdminEmailIsProvisionedAsAdminOnFirstLogin() {
        CatalogUser created = provisioningService.provisionOnLogin("seed.admin@example.com", "Seed Admin");

        assertThat(created.getRole()).isEqualTo(Role.ADMIN);
    }

    @TestConfiguration
    static class TestSeedAdminsConfig {
        @org.springframework.context.annotation.Bean
        SeedAdminsProperties seedAdminsProperties() {
            SeedAdminsProperties properties = new SeedAdminsProperties();
            properties.setSeedAdmins(java.util.List.of("seed.admin@example.com"));
            return properties;
        }
    }
}
