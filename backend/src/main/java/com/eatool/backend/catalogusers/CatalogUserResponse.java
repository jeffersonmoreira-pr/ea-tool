package com.eatool.backend.catalogusers;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model returned by the Catalog Users admin API (see issue #8): identity,
 * login method and current Role, without exposing anything sensitive.
 */
public record CatalogUserResponse(
        UUID id, String name, String email, Role role, LoginMethod loginMethod, Instant createdAt) {

    public static CatalogUserResponse from(CatalogUser user) {
        return new CatalogUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getLoginMethod(),
                user.getCreatedAt());
    }
}
