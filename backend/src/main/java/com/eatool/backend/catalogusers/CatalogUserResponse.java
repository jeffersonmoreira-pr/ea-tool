package com.eatool.backend.catalogusers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Read model returned by the Catalog Users admin API (see issue #8): identity,
 * login method, current Role and the assigned Access Scope (issue #10), without
 * exposing anything sensitive.
 */
public record CatalogUserResponse(
        UUID id,
        String name,
        String email,
        Role role,
        LoginMethod loginMethod,
        Instant createdAt,
        List<UUID> scopedDepartmentIds,
        List<UUID> scopedBusinessAreaIds) {

    public static CatalogUserResponse from(CatalogUser user) {
        return new CatalogUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getLoginMethod(),
                user.getCreatedAt(),
                List.copyOf(user.getScopedDepartmentIds()),
                List.copyOf(user.getScopedBusinessAreaIds()));
    }
}
