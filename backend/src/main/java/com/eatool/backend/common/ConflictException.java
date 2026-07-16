package com.eatool.backend.common;

/**
 * A request could not be completed because it would violate a uniqueness
 * rule (duplicate name) or because it would delete a master-data record
 * still referenced by an Application. Mapped to HTTP 409 by
 * {@link ApiExceptionHandler}.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
