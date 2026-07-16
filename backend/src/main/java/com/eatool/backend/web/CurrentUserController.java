package com.eatool.backend.web;

import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the currently authenticated user's identity so the frontend can
 * display it (e.g. in the sidebar), offer a logout action, and gate the
 * Admin-only Catalog Users screen by Role (see issue #8).
 */
@RestController
public class CurrentUserController {

    @GetMapping("/api/me")
    public Map<String, String> me(@AuthenticationPrincipal OidcUser oidcUser) {
        return Map.of(
                "email", oidcUser.getEmail() != null ? oidcUser.getEmail() : "",
                "name", oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getPreferredUsername(),
                "role", resolveRole(oidcUser));
    }

    private String resolveRole(OidcUser oidcUser) {
        return oidcUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("");
    }
}
