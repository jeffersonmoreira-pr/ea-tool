package com.eatool.backend.web;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.security.CurrentUserService;

/**
 * Exposes the currently authenticated user's identity so the frontend can
 * display it (e.g. in the sidebar), offer a logout action, and gate the
 * Admin-only Catalog Users screen by Role (see issue #8). Identity is resolved
 * independently of the login method (SSO or Local Login, see issue #9).
 */
@RestController
public class CurrentUserController {

    private final CurrentUserService currentUserService;
    private final CatalogUserRepository catalogUserRepository;

    public CurrentUserController(
            CurrentUserService currentUserService, CatalogUserRepository catalogUserRepository) {
        this.currentUserService = currentUserService;
        this.catalogUserRepository = catalogUserRepository;
    }

    @GetMapping("/api/me")
    public Map<String, String> me(Authentication authentication) {
        String email = currentUserService.email(authentication);
        return Map.of(
                "email", email != null ? email : "",
                "name", resolveName(authentication, email),
                "role", currentUserService.role(authentication));
    }

    private String resolveName(Authentication authentication, String email) {
        String displayName = currentUserService.displayName(authentication);
        if (displayName != null) {
            return displayName;
        }
        if (email != null) {
            return catalogUserRepository.findByEmail(email)
                    .map(CatalogUser::getName)
                    .orElse(email);
        }
        return "";
    }
}
