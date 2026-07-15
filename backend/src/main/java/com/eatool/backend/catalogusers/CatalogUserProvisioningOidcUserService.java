package com.eatool.backend.catalogusers;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Hooks into the OIDC login flow to provision a Catalog User (see
 * CatalogUserProvisioningService) right after Keycloak authenticates the
 * user, before the session is established.
 */
@Service
public class CatalogUserProvisioningOidcUserService extends OidcUserService {

    private final CatalogUserProvisioningService provisioningService;

    public CatalogUserProvisioningOidcUserService(CatalogUserProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getPreferredUsername();
        provisioningService.provisionOnLogin(email, name);
        return oidcUser;
    }
}
