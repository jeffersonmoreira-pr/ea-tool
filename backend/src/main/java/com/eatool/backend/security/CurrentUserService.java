package com.eatool.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

/**
 * Resolves the current user's identity and Role from the Spring Security
 * {@link Authentication}, regardless of how they authenticated (issue #9,
 * ADR-0004). SSO logins carry an {@link OidcUser} principal, while Local Login
 * logins carry a {@link UserDetails} principal; both expose the same email and
 * {@code ROLE_*} authorities, so authorization (Role, Access Scope, Edit
 * Permission) stays independent of the login method.
 */
@Component
public class CurrentUserService {

    /**
     * The authenticated user's email, used as the stable key to look up their
     * Catalog User record. For SSO this is the OIDC {@code email} claim; for
     * Local Login the username is the email.
     */
    public String email(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    /** A best-effort display name; null for principals that carry no name. */
    public String displayName(Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getPreferredUsername();
        }
        return null;
    }

    public boolean isAdmin(Authentication authentication) {
        return hasAuthority(authentication, "ROLE_ADMIN");
    }

    /** The Role name (e.g. {@code ADMIN}) derived from the {@code ROLE_*} authority, or empty. */
    public String role(Authentication authentication) {
        if (authentication == null) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("");
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
