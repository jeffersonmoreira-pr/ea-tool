package com.eatool.backend.web;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the currently authenticated user's identity so the frontend can
 * display it (e.g. in the sidebar) and offer a logout action.
 */
@RestController
public class CurrentUserController {

    @GetMapping("/api/me")
    public Map<String, String> me(@AuthenticationPrincipal OidcUser oidcUser) {
        return Map.of(
                "email", oidcUser.getEmail() != null ? oidcUser.getEmail() : "",
                "name", oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getPreferredUsername());
    }
}
