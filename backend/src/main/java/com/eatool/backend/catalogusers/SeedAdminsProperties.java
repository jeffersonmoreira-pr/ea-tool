package com.eatool.backend.catalogusers;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable list of emails that get provisioned as Admin on first login
 * instead of the default Viewer Role. Configurable via application.yml
 * (app.seed-admins) or the SEED_ADMIN_EMAILS environment variable, no code
 * change required.
 */
@ConfigurationProperties(prefix = "app")
public class SeedAdminsProperties {

    private List<String> seedAdmins = List.of();

    public List<String> getSeedAdmins() {
        return seedAdmins;
    }

    public void setSeedAdmins(List<String> seedAdmins) {
        this.seedAdmins = seedAdmins;
    }
}
