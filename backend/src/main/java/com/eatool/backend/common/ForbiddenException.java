package com.eatool.backend.common;

/**
 * The caller is authenticated but not allowed to perform this action on this
 * record. Mapped to HTTP 403 by {@link ApiExceptionHandler}. Used for
 * fine-grained Edit Permission enforcement (issue #11): an Editor without an
 * Edit Permission on a specific record may not edit or delete it.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
