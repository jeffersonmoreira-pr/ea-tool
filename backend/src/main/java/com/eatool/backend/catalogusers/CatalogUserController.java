package com.eatool.backend.catalogusers;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eatool.backend.common.BadRequestException;
import com.eatool.backend.common.ConflictException;
import com.eatool.backend.common.NotFoundException;

/**
 * Admin-only API backing the Catalog Users management screen (see issue #8):
 * list every provisioned user with their login method and Role, and change
 * another user's Role. Access is restricted to Admins centrally in
 * SecurityConfig (hasRole("ADMIN") on /api/catalog-users/**).
 */
@RestController
@RequestMapping("/api/catalog-users")
public class CatalogUserController {

    private final CatalogUserRepository catalogUserRepository;

    public CatalogUserController(CatalogUserRepository catalogUserRepository) {
        this.catalogUserRepository = catalogUserRepository;
    }

    @GetMapping
    public List<CatalogUserResponse> list() {
        return catalogUserRepository.findAll().stream()
                .sorted(Comparator.comparing(CatalogUser::getCreatedAt))
                .map(CatalogUserResponse::from)
                .toList();
    }

    @PutMapping("/{id}/role")
    public CatalogUserResponse changeRole(
            @PathVariable UUID id,
            @RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal OidcUser currentUser) {
        Role newRole = parseRole(request.getRole());
        CatalogUser user = catalogUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Catalog User not found: " + id));

        if (isSelf(user, currentUser)) {
            throw new ConflictException("You cannot change your own Role.");
        }

        user.setRole(newRole);
        return CatalogUserResponse.from(catalogUserRepository.save(user));
    }

    private boolean isSelf(CatalogUser user, OidcUser currentUser) {
        String currentEmail = currentUser == null ? null : currentUser.getEmail();
        return currentEmail != null && currentEmail.equalsIgnoreCase(user.getEmail());
    }

    private Role parseRole(String rawRole) {
        String value = rawRole == null ? "" : rawRole.trim();
        if (value.isEmpty()) {
            throw new BadRequestException("Role is required.");
        }
        try {
            return Role.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw new BadRequestException("Role must be VIEWER, EDITOR, or ADMIN.");
        }
    }
}
