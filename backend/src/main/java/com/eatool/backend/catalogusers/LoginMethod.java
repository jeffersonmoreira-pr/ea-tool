package com.eatool.backend.catalogusers;

/**
 * How a Catalog User authenticates (see CONTEXT.md and ADR-0004): SSO via the
 * corporate OIDC provider, or a locally managed username/password account
 * (Local Login) for break-glass access and external partners.
 */
public enum LoginMethod {
    SSO,
    LOCAL
}
