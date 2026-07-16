package com.eatool.backend.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Test logins carrying a Catalog User Role authority, mirroring what
 * CatalogUserProvisioningOidcUserService stamps on the real principal, so
 * controller tests can exercise Role-based authorization (see issue #6).
 */
public final class OidcLogins {

    private OidcLogins() {
    }

    public static RequestPostProcessor viewerLogin() {
        return oidcLogin().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"));
    }

    public static RequestPostProcessor editorLogin() {
        return oidcLogin().authorities(new SimpleGrantedAuthority("ROLE_EDITOR"));
    }

    public static RequestPostProcessor adminLogin() {
        return oidcLogin().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
