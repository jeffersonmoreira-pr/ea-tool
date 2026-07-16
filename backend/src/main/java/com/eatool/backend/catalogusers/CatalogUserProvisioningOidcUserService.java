package com.eatool.backend.catalogusers;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Hooks into the OIDC login flow to provision a Catalog User (see
 * CatalogUserProvisioningService) right after Keycloak authenticates the
 * user, before the session is established.
 *
 * Also stamps the authenticated principal with the Catalog User's Role as a
 * Spring Security authority (ROLE_VIEWER/ROLE_EDITOR/ROLE_ADMIN), so the
 * SecurityConfig can enforce write authorization by Role (see issue #6).
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
        CatalogUser catalogUser = provisioningService.provisionOnLogin(email, name);

        Set<GrantedAuthority> authorities = new LinkedHashSet<>(oidcUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + catalogUser.getRole().name()));
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
