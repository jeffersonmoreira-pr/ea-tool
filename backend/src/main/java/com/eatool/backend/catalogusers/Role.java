package com.eatool.backend.catalogusers;

/**
 * Local authorization level assigned to a Catalog User (see CONTEXT.md):
 * Viewer (read-only), Editor (create/edit/delete Applications and master
 * data), or Admin (Editor plus managing Catalog Users and Roles).
 */
public enum Role {
    VIEWER,
    EDITOR,
    ADMIN
}
